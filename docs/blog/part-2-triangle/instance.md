---
title: The Vulkan Instance
---

---

## Contents

- [Overview](#overview)
- [Instance](#vulkan-instance)
- [Diagnostics Handlers](#diagnostics-handler)
- [Testing Issues](#testing-issues)

---

## Overview

The first step in the development of JOVE is to create the Vulkan _instance_ - the starting point for everything that follows.

Creating the instance involves the following steps:

1. Instantiate the Vulkan API using JNA.

2. Specify the API extensions we require for our application.

3. Configure a JNA structure descriptor outlining our requirements for the instance.

4. Invoke the API to create the instance given this descriptor.

For this we will require the following components:

* Implementation of a JNA library to create/destroy the instance.

* An instance domain object.

* The relevant code generated structures used to specify the requirements of the instance.

As already mentioned in the [code generation](/JOVE/blog/part-1-intro/generation) chapter we will employ the GLFW library which provides services for managing windows, input devices, etc. that we will use in future chapters (the tutorial also uses GLFW).  However another compelling reason to use GLFW is that it also offers functionality to create a Vulkan rendering surface suitable for the platform on which the application is executed.

We _could_ use Vulkan extensions to implement the surface from the ground up but it makes sense to take advantage of the platform-independant implementation.  The disadvantage of this approach is that the logic becomes a little convoluted as the surface and Vulkan components are slightly inter-dependant, but this seems an acceptable trade-off.

To create an instance for a given platform we therefore also require:

* A second JNA library for GLFW.

* Additional domain objects for extensions and validation layers.

We will also implement the diagnostics extension to support logging and error reporting which will become _very_ helpful in subsequent chapters.

Finally we will address some issues around unit-testing Vulkan and JNA based code.

---

## Vulkan Instance

### Vulkan Library

We start by defining the Vulkan library for management of the instance:

```java
interface VulkanLibrary extends Library {
    /**
     * Creates a vulkan instance.
     * @param info             Instance descriptor
     * @param allocator        Allocator
     * @param instance         Returned instance
     * @return Result
     */
    int vkCreateInstance(VkInstanceCreateInfo info, Pointer allocator, PointerByReference instance);

    /**
     * Destroys the vulkan instance.
     * @param instance         Instance handle
     * @param allocator        Allocator
     */
    void vkDestroyInstance(Pointer instance, Pointer allocator);
}
```

This JNA interface maps to the following methods defined in the `vulkan_core.h` header:

```java
typedef VkResult (VKAPI_PTR *PFN_vkCreateInstance)(const VkInstanceCreateInfo* pCreateInfo, const VkAllocationCallbacks* pAllocator, VkInstance* pInstance);
typedef void (VKAPI_PTR *PFN_vkDestroyInstance)(VkInstance instance, const VkAllocationCallbacks* pAllocator);
```

Notes:

* As detailed in the previous chapter we have intentionally decided to hand-craft the native methods rather than attempting to code-generate the API.

* We only need to implement the two API methods we actually need at this stage.

* The handle to the newly created instance is returned as a JNA `PointerByReference` object which maps to a native `VkInstance*` return-by-reference type.

* The `pAllocator` parameter in the API methods is out-of-scope for our library and is always set to `null`.

To instantiate the API itself we add the following factory method to the library:

```java
static VulkanLibrary create() {
    String name = switch(Platform.getOSType()) {
        case Platform.WINDOWS -> "vulkan-1";
        case Platform.LINUX -> "libvulkan";
        default -> throw new UnsupportedOperationException(...);
    }

    return Native.load(name, VulkanLibrary.class);
}
```

### Instance

With the library in place we can implement our first domain object:

```java
public class Instance {
    private final VulkanLibrary lib;
    private final Pointer handle;

    private Instance(VulkanLibrary lib, Pointer handle) {
        this.api = notNull(lib);
        this.handle = notNull(handle);
    }

    VulkanLibrary library() {
        return lib;
    }

    public Pointer handle() {
        return handle;
    }

    public void destroy() {
        lib.vkDestroyInstance(handle, null);
    }
}
```

Creating the instance involves populating a couple of JNA structures and invoking the create API method - this is an ideal scenario for a builder:

```java
public static class Builder {
    private String name;
    private Version ver = new Version(1, 0, 0);

    public Builder name(String name) {
        this.name = notEmpty(name);
        return this;
    }

    public Builder version(Version ver) {
        this.ver = notNull(ver);
        return this;
    }

    public Instance build(VulkanLibrary lib) {
        ...
    }
}
```

The `Version` number is a simple record:

```java
public record Version(int major, int minor, int patch) implements Comparable<Version> {
    /**
     * @return Packed version integer
     */
    public int toInteger() {
        return (major << 22) | (minor << 12) | patch;
    }

    @Override
    public int compareTo(Version that) {
        return this.toInteger() - that.toInteger();
    }
}
```

In the `build` method we use the code-generated structures for the first time to populate the application and engine details:

```java
public Instance build(VulkanLibrary lib) {
    VkApplicationInfo app = new VkApplicationInfo();
    app.pApplicationName = name;
    app.applicationVersion = ver.toInteger();
    app.pEngineName = "JOVE";
    app.engineVersion = new Version(1, 0, 0).toInteger();
    app.apiVersion = VulkanLibrary.VERSION.toInteger();
    ...
}
```

The `apiVersion` field is the __maximum__ version of the Vulkan API that the application can use - we add a new constant to the API for the version supported by the SDK:

```java
public interface VulkanLibrary {
    Version VERSION = new Version(1, 1, 0);
}
```

Next we populate the descriptor for the instance (which only consists of the application details for the moment):

```java
var info = new VkInstanceCreateInfo();
info.pApplicationInfo = app;
```

Finally we invoke the API method to create the instance and construct the domain object:

```java
// Create instance
PointerByReference handle = new PointerByReference();
check(api.vkCreateInstance(info, null, handle));

// Create instance wrapper
return new Instance(api, handle.getValue());
```

The `check` method wraps an API call and validates the result code:

```java
public interface VulkanLibrary {
    int SUCCESS = VkResult.SUCCESS.value();
    
    static void check(int result) {
        if(result != SUCCESS) {
            throw new VulkanException(result);
        }
    }
}
```

The `VulkanException` is a custom exception class that builds an informative message for a Vulkan error code:

```java
public class VulkanException extends RuntimeException {
    public final int result;

    public VulkanException(int result) {
        super(String.format("%s[%d]", reason(result), result));
        this.result = result;
    }

    private static String reason(int result) {
        try {
            return IntegerEnumeration.mapping(VkResult.class).map(result).name();
        }
        catch(IllegalArgumentException e) {
            return "Unknown error code";
        }
    }
}
```

### Extensions and Validation Layers

There are two other pieces of information to be supplied when creating the instance: extensions and validation layers.

An _extension_ is a platform-specific extension to the Vulkan API, e.g. swapchain support, native window surfaces, etc.

A _validation layer_ is a hook or interceptor that adds additional functionality to the API (usually diagnostics) such as parameter validation, resource leak detection, etc.

To support these we add two new properties to the builder:

```java
public static class Builder {
    private final Set<String> extensions = new HashSet<>();
    private final Set<String> layers = new HashSet<>();
    ...

    public Builder extension(String ext) {
        Check.notEmpty(ext);
        extensions.add(ext);
        return this;
    }

    public Builder layer(ValidationLayer layer) {
        Check.notNull(layer);
        layers.add(layer.name());
        return this;
    }
}
```

The relevant fields of the instance descriptor are populated in the `build` method (which were left as defaults in the previous iteration):

```java
public Instance build(VulkanLibrary lib) {
    ...
    
    // Populate required extensions
    info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
    info.enabledExtensionCount = extensions.size();
    
    // Populate required layers
    info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
    info.enabledLayerCount = layers.size();
    
    ...
}
```

Note the use of the JNA `StringArray` helper class that maps a Java array-of-strings to a native pointer-to-pointers (more specifically a `const char* const*` type).

Finally we introduce a simple class for validation layers:

```java
public record ValidationLayer(String name, int version) {
    /**
     * Standard validation layer.
     */
    public static final ValidationLayer STANDARD_VALIDATION = new ValidationLayer("VK_LAYER_LUNARG_standard_validation");
}
```

We discuss the purpose of the standard validation layer below.

### Required Extensions

Generally we will need to enable platform-specific extensions to actually perform rendering.

Usually there will be two extensions for a Vulkan rendering surface:
- the general surface: `VK_KHR_surface` 
- and the platform specific implementation, e.g. `VK_KHR_win32_surface` for Windows.

This is where we introduce a new package and JNA interface for the GLFW library:

```java
interface DesktopLibrary extends Library {
    /**
     * Initialises GLFW.
     * @return Success code
     */
    int glfwInit();

    /**
     * Terminates GLFW.
     */
    void glfwTerminate();

    /**
     * @return Whether Vulkan is supported on this platform
     */
    boolean glfwVulkanSupported();

    /**
     * Enumerates the required Vulkan extensions for this platform.
     * @param count Number of results
     * @return Vulkan extensions (pointer to array of strings)
     */
    Pointer glfwGetRequiredInstanceExtensions(IntByReference count);
}
```

Next we create the _desktop_ service that abstracts over the underlying GLFW implementation:

```java
public class Desktop {
    public static Desktop create() {
        ...
    }

    private final DesktopLibrary lib;

    Desktop(DesktopLibrary lib) {
        this.lib = notNull(lib);
    }

    public boolean isVulkanSupported() {
        return lib.glfwVulkanSupported();
    }

    public String[] extensions() {
        IntByReference size = new IntByReference();
        Pointer ptr = lib.glfwGetRequiredInstanceExtensions(size);
        return ptr.getStringArray(0, size.getValue());
    }
}
```

The service is created and initialised in a similar fashion to the Vulkan API:

```java
public static Desktop create() {
    // Determine library name
    String name = switch(Platform.getOSType()) {
        case Platform.WINDOWS -> "glfw3";
        case Platform.LINUX -> "libglfw";
        default -> throw new UnsupportedOperationException(...);
    };

    // Load native library
    DesktopLibrary lib = Native.load(name, DesktopLibrary.class);

    // Init GLFW
    int result = lib.glfwInit();
    if(result != 1) throw new RuntimeException(...);

    // Create desktop
    return new Desktop(lib);
}
```

Notes:

* The new package is called `desktop` rather than using an ugly GLFW acronym.

* In reality the new GLFW framework was already in place before this stage of the tutorial, but we will continue to introduce the functionality as though it was developed at the time, as it becomes relevant.

### Integration

Finally we can create our first demo application to instantiate a Vulkan instance for the local hardware:

```java
public class InstanceDemo {
    public static void main(String[] args) throws Exception {
        // Open desktop
        Desktop desktop = Desktop.create();
        if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");

        // Lookup required extensions
        String[] extensions = desktop.extensions();

        // Init Vulkan
        VulkanLibrary lib = VulkanLibrary.create();

        // Create instance
        Instance instance = new Instance.Builder()
            .name("InstanceDemo")
            .extensions(desktop.extensions())
            .build(lib);

        // Cleanup
        instance.destroy();
        desktop.destroy();
    }
}
```

We also add a convenience method to the builder to add the array of extensions returned by GLFW.

---

## Diagnostics Handler

### Overview

Vulkan implements the `STANDARD_VALIDATION` layer that provides an excellent error and diagnostics reporting mechanism, offering comprehensive logging as well as identifying common problems such as orphaned object handles, invalid parameters, performance warnings, etc.  This functionality is not mandatory but its safe to say it is _highly_ recommended during development, so we will address it now before we progress any further.

However there is a complication - the reporting mechanism is not a core part of the API but is itself an extension.  The relevant function pointers must be looked up from the instance and the associated data structures must be determined from the Vulkan documentation.

Registering a diagnostics handler consists of the following steps:

1. Specify the diagnostics reporting requirements in a descriptor.

2. Create a JNA callback to be invoked by Vulkan to report a diagnostics message.

3. Lookup the function pointer to create the handler.

4. Invoke the create function with the descriptor and callback as parameters to attach the handler to the instance.

Our design for the the process of reporting a message is as follows:

1. Vulkan invokes the callback to report a diagnostics message.

2. The callback parameters are aggregated into a message record.

3. Which is delegated to a consumer for the application (which by default dumps the message to the error console).

To implement all the above we require the following components:

* A builder to specify the diagnostics reporting requirements for the application.

* Additional functionality to lookup the function pointers to create and destroy handlers.

* The JNA callback invoked by Vulkan to report messages.

* A `Message` record that aggregates the diagnostics report.

Note that we will still attempt to implement comprehensive argument and logic validation throughout JOVE to trap errors at source.  Although this means we are essentially replicating the validation layer, the development overhead is usually worth the effort on the assumption it will be considerably easier for the developer to diagnose an exception stack-trace as opposed to an error message with limited context.

### Handler Manager

We start with a new component that is responsible for managing diagnostics handlers on behalf of the instance:

```java
public class HandlerManager {
    private final Instance instance;
    private final Function create, destroy;
    private final Collection<Handler> handlers = new ArrayList<>();
}
```

In the constructor we instantiate the JNA function pointers to create and destroy handlers:

```java
HandlerManager(Instance instance) {
    this.instance = notNull(instance);
    this.create = instance.function("vkCreateDebugUtilsMessengerEXT");
    this.destroy = instance.function("vkDestroyDebugUtilsMessengerEXT");
}
```

The function pointers are looked up by the following new method on the instance class:

```java
public Function function(String name) {
    Pointer ptr = lib.vkGetInstanceProcAddr(this, name);
    if(ptr == null) throw new RuntimeException(...);
    return Function.getFunction(ptr);
}
```

And the API method is added to the library:

```java
interface VulkanLibrary {
    /**
     * Looks up an instance function.
     * @param instance      Vulkan instance
     * @param name          Function name
     * @return Function pointer
     */
    Pointer vkGetInstanceProcAddr(Pointer instance, String name);
}
```

### Handler Builder

A new handler is configured and attached to the instance using a builder:

```java
public class Builder {
    private final Set<VkDebugUtilsMessageSeverity> severity = new HashSet<>();
    private final Set<VkDebugUtilsMessageType> types = new HashSet<>();
    private Consumer<Message> consumer = System.err::println;
}
```

The `build` method first populates the descriptor for the diagnostics handler:

```java
public Handler build() {
    var info = new VkDebugUtilsMessengerCreateInfoEXT();
    info.messageSeverity = IntegerEnumeration.mask(severity);
    info.messageType = IntegerEnumeration.mask(types);
    info.pfnUserCallback = new MessageCallback(consumer);
    info.pUserData = null;
    ...
    
    return handler;
}
```

Next the the function to create the handler is invoked with the appropriate arguments (specified by the documentation):

```java
Pointer parent = instance.handle();
PointerByReference ref = instance.factory().pointer();
Object[] args = {parent, info, null, ref};
check(create.invokeInt(args));
```

Finally we create the handler domain object and register it with the manager:

```java
Handler handler = new Handler(ref.getValue());
handlers.add(handler);
```

Notes:

* The JNA `invokeInt` method calls a function pointer with an arbitrary array of arguments.

* The name of the function is specified in the [Vulkan documentation](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#vkCreateDebugUtilsMessengerEXT).

* We maintain a list of the attached `handlers` which are released when we destroy the instance (see below).

The handler domain object itself is trivial:

```java
public class Handler {
    private final Pointer handle;

    private Handler(Pointer handle) {
        this.handle = notNull(handle);
    }

    void destroy() {
        // Destroy handler
        Pointer parent = instance.handle();
        Object[] args = {parent, this.handle, null};
        destroy.invoke(args);

        // Remove handler
        assert handlers.contains(this);
        handlers.remove(this);
    }
}
```

Finally attached handlers are released in the manager when the instance is destroyed:

```java
public synchronized void close() {
    List.copyOf(handlers).forEach(Handler::destroy);
    assert handlers.isEmpty();
}
```

Note that this code copies the list of registered handlers to avoid concurrent modification.

### Message Callback

The message domain object is a simple inner record class:

```java
public record Message(
    VkDebugUtilsMessageSeverity severity,
    Collection<VkDebugUtilsMessageType> types,
    VkDebugUtilsMessengerCallbackData data
)
```

The callback invoked by Vulkan to report messages is a JNA callback:

```java
static class MessageCallback implements Callback {
    private final Consumer<Message> consumer;

    MessageCallback(Consumer<Message> consumer) {
        this.consumer = consumer;
    }

    /**
     * Callback handler method.
     * @param severity          Severity
     * @param type              Message type(s) mask
     * @param pCallbackData     Data
     * @param pUserData         Optional user data (always {@code null})
     * @return Whether to continue execution (always {@code false})
     */
    public boolean message(int severity, int type, VkDebugUtilsMessengerCallbackData pCallbackData, Pointer pUserData) {
        ...
        return false;
    }
}
```
Notes:

* The signature of the callback method is derived from the documentation (as an extension it is not part of the API).

* A JNA callback is an interface that must contain a __single__ public method, but this is not enforced at compile-time.

* The `pUserData` parameter is optional data returned to the callback to correlate state, this is largely redundant for an OO application and is always `null` in our implementation.

The `message` callback method first transforms the _severity_ and _types_ bit fields to the corresponding enumerations:

```java
VkDebugUtilsMessageSeverity severityEnum = IntegerEnumeration.mapping(VkDebugUtilsMessageSeverity.class).map(severity);
Collection<VkDebugUtilsMessageType> typesEnum = IntegerEnumeration.mapping(VkDebugUtilsMessageType.class).enumerate(type);
```

And the relevant data is composed into a message instance and delegated to the handler:

```java
Message message = new Message(severityEnum, typesEnum, pCallbackData);
consumer.accept(message);
```

We also add a custom `toString` implementation to the `Message` record to build a human-readable representation of a diagnostics report:

```java
public String toString() {
    String compoundTypes = types.stream().map(Enum::name).collect(joining("-"));
    StringJoiner str = new StringJoiner(":");
    str.add(severity.name());
    str.add(compoundTypes);
    if(!data.pMessage.contains(data.pMessageIdName)) {
        str.add(data.pMessageIdName);
    }
    str.add(data.pMessage);
    return str.toString();
}
```

For example (excluding the message text):

```
ERROR:VALIDATION:Validation Error: [ VUID-VkDeviceQueueCreateInfo-pQueuePriorities-00383 ] ...
```

### Integration

To attach a diagnostics handler we must first enable the extension and register the standard validation layer when creating the instance:

```java
Instance instance = new Instance.Builder()
    .name("InstanceDemo")
    .extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
    .extensions(desktop.extensions())
    .layer(ValidationLayer.STANDARD_VALIDATION)
    .build(lib);
```

The diagnostics extension is defined in the main library:

```java
public interface VulkanLibrary {
    String EXTENSION_DEBUG_UTILS = "VK_EXT_debug_utils";
}
```

In the demo application we can now configure and attach a diagnostics handler:

```java
instance
    .manager()
    .builder()
    .severity(VkDebugUtilsMessageSeverity.WARNING)
    .severity(VkDebugUtilsMessageSeverity.ERROR)
    .type(VkDebugUtilsMessageType.GENERAL)
    .type(VkDebugUtilsMessageType.VALIDATION)
    .build();
```

From now on when we screw things up we should receive error messages on the console.

---

## Testing Issues

### Background

To unit-test the code that creates the instance we might start with the following (naive) approach:

```java
private VulkanLibrary lib;

@BeforeEach
void before() {
    lib = mock(VulkanLibrary.class);
}

@Test
void build() {
    // Init expected create descriptor
    var expected = new VkInstanceCreateInfo();
    ...

    // Init create method
    var handle = new PointerByReference();
    when(lib.vkCreateInstance(expected, null, handle)).thenReturn(0);

    // Build instance
    Instance instance = new Instance.Builder()
        .name("test")
        .extension("extension")
        .build(lib);

    // Check created instance        
    assertNotNull(instance);
    assertEquals(handle.getValue(), instance.handle());
    ...
}
```

The test is designed to check that the builder invokes the correct API method with the expected arguments (including the `VkInstanceCreateInfo` descriptor).

However by default all methods in the mocked library will return zero (which of course is the Vulkan success return code) so the test passes whether we include the `when` clause or not and essentially proves nothing.

A better approach is to introduce a post-condition that explicitly validates the expected method invocation:

```java
verify(lib).vkCreateInstance(expected, null, handle);
```

However this introduces further problems:

### Reference Types

The Vulkan API (and most other native libraries) make extensive use of _by-reference_ types to return data, with the actual return value of the method generally representing an error code (since base C does not support the notion of exceptions).  For example the `vkCreateInstance` API method returns the `handle` of the newly created instance via a JNA `PointerByReference` type.

Mercifully this approach is virtually unknown in Java but it does pose an awkward problem when we come to testing - the usual Java unit-testing frameworks (JUnit, Mockito) are designed around the return value of a method generally being the important part and any error conditions modelled by exceptions.

If we pass a new `PointerByReference` created in the test it will be a different instance to the one created in the `build` method itself - it would be difficult to determine whether the test was legitimately successful or only passed by luck.

We could change the `verify` statement to use Mockito matchers:

```java
verify(lib).vkCreateInstance(..., isA(PointerByReference.class));
```

But this only allows us to check that the argument was not `null` rather than verifying the actual value.  Additionally __every__ argument would then have to be a Mockito argument matcher which just adds more development complexity and obfuscates the test.

We could alternatively use a Mockito _answer_ for the handle to initialise the argument, but that would require tedious and repetitive code for _every_ unit-test that exercises API methods with by-reference return values (which is pretty much all of them).

To resolve (or at least mitigate) this issue we introduce the _reference factory_ that is responsible for generated by-reference objects used in API calls:

```java
public interface ReferenceFactory {
    /**
     * @return New integer-by-reference
     */
    IntByReference integer();

    /**
     * @return New pointer-by-reference
     */
    PointerByReference pointer();
}
```

We can now mock the reference factory and/or stub individual factory methods as appropriate for a given unit-test.

The default implementation simply creates new instances on demand:

```java
public interface ReferenceFactory {
    ...
    ReferenceFactory DEFAULT = new ReferenceFactory() {
        @Override
        public IntByReference integer() {
            return new IntByReference();
        }

        @Override
        public PointerByReference pointer() {
            return new PointerByReference();
        }
    };
}
```

The builder for the instance is modified to retrieve a pointer reference from the factory:

```java
public static class Builder {
    private ReferenceFactory factory = ReferenceFactory.DEFAULT;
    ...

    public Instance build(VulkanLibrary lib) {
        ...
        // Create instance
        PointerByReference ref = factory.pointer();
        check(lib.vkCreateInstance(info, null, ref));
    
        // Create instance domain wrapper
        return new Instance(handle.getValue(), lib, factory);
    }
}
```

### Structure Equality

We also want to validate that the code constructs the expected Vulkan structures to be passed to the API method.  Unfortunately it turns out that two JNA structures with the same data are __not__ equal and the above `verify` test fails even though the code is working correctly.  A review of the JNA source code shows that the structure class essentially violates the Java `equals` contract assumed by the testing frameworks.

A JNA structure provides the `dataEquals` method but this compares the native representation of the structure rather than the fields themselves.  Additionally the developer is also required to ensure that both structure have been written to native memory which is fiddly and error prone.

We could use a Mockito _argument captor_ to intercepts and check the data passed to the API method, but again this means __all__ arguments must be matchers and adds another layer of complexity, just to get around the fact that JNA structures do not support equality as expected.

Instead we take the simpler (if more dubious) option of bastardising the `equals` method of the expected structure to validate the actual data:

```java
var expected = new VkInstanceCreateInfo() {
    @Override
    public boolean equals(Object obj) {
        // Check instance descriptor
        var info = (VkInstanceCreateInfo) obj;
        assertEquals(1, info.enabledExtensionCount);
        ...

        // Check application descriptor
        VkApplicationInfo app = info.pApplicationInfo;
        assertNotNull(app);
        assertEquals("test", app.pApplicationName);
        ...

        return true;
    }
};
```

### Conclusion

Using the reference factory and the structure equality bodge the final unit-test looks like this:

```java
private VulkanLibrary lib;
private ReferenceFactory factory;

@BeforeEach
void before() {
    lib = mock(VulkanLibrary.class);
    factory = mock(ReferenceFactory.class);
    when(factory.pointer()).thenReturn(new PointerByReference(new Pointer(1)));
}

@Test
void build() {
    // Build instance
    Instance instance = new Instance.Builder()
        .name("test")
        .extension("extension")
        .factory(factory)
        .build(lib);

    // Check created instance        
    assertNotNull(instance);
    ...

    // Init expected create descriptor
    var expected = new VkInstanceCreateInfo() {
        ...
    };

    // Check API
    verify(lib).vkCreateInstance(expected, null, factory.pointer());
}
```

In general from now on we will not cover testing unless there is a specific point-of-interest, it can be assumed that unit-tests are developed in-parallel with the main code.

---

## Summary

In this first chapter we:

- Instantiated the Vulkan API and GLFW library.

- Created a Vulkan instance with the required extensions and layers.

- Attached a diagnostics handler.

- Mitigated some of the issues around unit-testing Vulkan API methods.
