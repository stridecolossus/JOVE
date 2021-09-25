---
title: The Vulkan Instance
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

As already mentioned in the [code generation](/JOVE/blog/part-1-generation/code-generation) chapter we will employ the GLFW library which provides services for managing windows, input devices, etc. that we will use in future chapters (the tutorial also uses GLFW).  However another compelling reason to use GLFW is that it also offers functionality to create a Vulkan rendering surface suitable for the platform on which the application is executed.

We _could_ use Vulkan extensions to do this from the ground up but it makes sense to take advantage of the platform-independant implementation making our code considerably simpler.  The disadvantage of this approach is that the logic becomes a little convoluted as the surface and Vulkan components are slightly inter-dependant, but this seems an acceptable trade-off.

To create an instance appropriate to the local hardware we therefore also require:

* A second JNA library for GLFW.

* Additional domain objects for extensions and validation layers.

We will also implement the diagnostics extension to support logging and error reporting (which will become _very_ helpful in subsequent chapters).

Finally we cover some minor improvements and discuss issues around unit-testing.

---



naive doesn't test method or any!
numbering
mention started to address testing to summary
should write ??? by VK result (???)
solutions mitigation after SO discussions




## Creating the Vulkan Instance

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

* We only implement the two API methods we actually need at this stage.

* Note that the handle to the newly created instance is returned as a JNA `PointerByReference` object which maps to a native `VkInstance*` return-by-reference type.

* The `pAllocator` parameter in the API methods is out-of-scope for our library and is always set to `null`.

To instantiate the API itself we add the following factory method to the library:

```java
static VulkanLibrary create() {
    final String name = switch(Platform.getOSType()) {
        case Platform.WINDOWS -> "vulkan-1";
        case Platform.LINUX -> "libvulkan";
        default -> throw new UnsupportedOperationException("Unsupported platform: " + Platform.getOSType());
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
    // Init application descriptor
    final VkApplicationInfo app = new VkApplicationInfo();
    app.pApplicationName = name;
    app.applicationVersion = ver.toInteger();
    app.pEngineName = "JOVE";
    app.engineVersion = new Version(1, 0, 0).toInteger();
    app.apiVersion = VulkanLibrary.VERSION.toInteger();
```

The `apiVersion` field is the **maximum** version of the Vulkan API that the application can use - we add a new constant to the API for the version supported by the SDK:

```java
public interface VulkanLibrary {
    Version VERSION = new Version(1, 1, 0);
}
```

Next we populate the descriptor for the instance (which only consists of the application details for the moment):

```java
    // Init instance descriptor
    final VkInstanceCreateInfo info = new VkInstanceCreateInfo();
    info.pApplicationInfo = app;
```

Finally we invoke the API method to create the instance and construct the domain object:

```java
    // Create instance
    final PointerByReference handle = new PointerByReference();
    check(api.vkCreateInstance(info, null, handle));
    
    // Create instance wrapper
    return new Instance(api, handle.getValue());
}
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

`VulkanException` is a custom exception class that builds an informative message for a Vulkan error code:

```java
public class VulkanException extends RuntimeException {
    public final int result;

    /**
     * Constructor.
     * @param result        Vulkan result code
     * @param message       Additional message
     */
    public VulkanException(int result, String message) {
        super(String.format("[%d]%s: %s", result, reason(result), message));
        this.result = result;
    }

    /**
     * Helper - Maps the given Vulkan result code to the corresponding reason token.
     * @param result Vulkan result code
     * @return Reason code
     */
    private static String reason(int result) {
        try {
            final VkResult value = IntegerEnumeration.map(VkResult.class, result);
            return value.name();
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

(We discuss the purpose of the standard validation layer below).

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
        final IntByReference size = new IntByReference();
        final Pointer ptr = lib.glfwGetRequiredInstanceExtensions(size);
        return ptr.getStringArray(0, size.getValue());
    }
}
```

The service is created and initialised in a similar fashion to the Vulkan API:

```java
public static Desktop create() {
    // Determine library name
    final String name = switch(Platform.getOSType()) {
        case Platform.WINDOWS -> "glfw3";
        case Platform.LINUX -> "libglfw";
        default -> throw new UnsupportedOperationException("Unsupported platform for GLFW: " + Platform.getOSType());
    };

    // Load native library
    final DesktopLibrary lib = Native.load(name, DesktopLibrary.class);

    // Init GLFW
    final int result = lib.glfwInit();
    if(result != 1) throw new RuntimeException("Cannot initialise GLFW: code=" + result);

    // Create desktop
    return new Desktop(lib);
}
```

### Integration

Finally we can create our first demo application to instantiate a Vulkan instance for the local hardware:

```java
public class InstanceDemo {
    public static void main(String[] args) throws Exception {
        // Open desktop
        final Desktop desktop = Desktop.create();
        if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");

        // Lookup required extensions
        final String[] extensions = desktop.extensions();

        // Init Vulkan
        final VulkanLibrary lib = VulkanLibrary.create();

        // Create instance
        final Instance instance = new Instance.Builder()
                .name("InstanceDemo")
                .extensions(desktop.extensions())
                .build(lib);

        // Cleanup
        instance.destroy();
        desktop.destroy();
    }
}
```

We also added a convenience method to the builder to add the array of extensions returned by GLFW.

---

## Diagnostics Handler

### Overview

Vulkan implements the `STANDARD_VALIDATION` layer that provides an excellent error and diagnostics reporting mechanism, offering comprehensive logging as well as identifying common problems such as orphaned object handles, invalid parameters, performance warnings, etc.  This functionality is not mandatory but its safe to say it is _highly_ recommended during development, so we will address it now before we progress any further.

However there is a complication - the reporting mechanism is not a core part of the API but is itself an extension: the relevant function pointers must be looked up from the instance and the associated data structures are determined from the Vulkan documentation.

Registering a diagnostics handler consists of the following steps:

1. Specify the diagnostics reporting requirements in a `VkDebugUtilsMessengerCreateInfo` descriptor.

2. Create a JNA callback to be invoked by Vulkan to report a message.

3. Lookup the `vkCreateDebugUtilsMessengerEXT` function pointer.

4. Invoke the create function with the descriptor and callback as parameters to attach the handler to the instance.

Our design for the the process of reporting a message is as follows:

1. Vulkan invokes the callback to report a diagnostics message.

2. The callback parameters are aggregated into a message POJO.

3. The message is delegated to a consumer for the given application (which by default will just dump the message to the error console).

To implement all the above we require the following components:

* A builder to specify the diagnostics reporting requirements for a given application.

* Additional functionality to lookup the function pointer that creates and attaches a message handler.

* The JNA callback invoked by Vulkan to report messages.

* A `Message` record that aggregates the diagnostics report.

We start with the following outline as a local class of the instance:

```java
public static class Handler {
    /**
     * A <i>message</i> is a diagnostics report generated by Vulkan.
     */
    public record Message(VkDebugUtilsMessageSeverity severity, Collection<VkDebugUtilsMessageType> types, VkDebugUtilsMessengerCallbackData data) {
    }

    /**
     * Message callback.
     */
    static class MessageCallback implements Callback {
    }
    
    /**
     * Attaches this handler to the given instance.
     * @param instance Instance
     */
    public void attach(Instance instance) {
    }
}
```

### Message Handler

The `Handler` is a builder used to specify the message consumer and the severity and types of message of interest:

```java
public static class Handler {
    private final Set<VkDebugUtilsMessageSeverity> severity = new HashSet<>();
    private final Set<VkDebugUtilsMessageType> types = new HashSet<>();
    private Consumer<Message> consumer = System.err::println;

    /**
     * Sets the message consumer (dumps messages to the error console by default).
     * @param consumer Message consumer
     */
    public Handler consumer(Consumer<Message> consumer) {
        this.consumer = notNull(consumer);
        return this;
    }

    /**
     * Adds a message severity to be reported by this handler.
     * @param severity Message severity
     */
    public Handler severity(VkDebugUtilsMessageSeverity severity) {
        this.severity.add(notNull(severity));
        return this;
    }

    /**
     * Adds a message type to be reported by this handler.
     * @param type Message type
     */
    public Handler type(VkDebugUtilsMessageType type) {
        types.add(notNull(type));
        return this;
    }
}
```

The `attach` method populates the creation descriptor and delegates to a new private helper on the instance:

```java
public void attach(Instance instance) {
    // Create callback
    final MessageCallback callback = new MessageCallback(consumer);

    // Build handler descriptor
    final VkDebugUtilsMessengerCreateInfoEXT info = new VkDebugUtilsMessengerCreateInfoEXT();
    info.messageSeverity = IntegerEnumeration.mask(severity);
    info.messageType = IntegerEnumeration.mask(types);
    info.pfnUserCallback = callback;
    info.pUserData = null;

    // Attach to instance
    instance.attach(info);
}
```

This delegate method looks up and invokes the function pointer to create and attach the handler:

```java
public class Instance {
    private final Collection<Pointer> handlers = new ArrayList<>();

    private void attach(VkDebugUtilsMessengerCreateInfoEXT info) {
        // Lookup create function
        final Function create = function("vkCreateDebugUtilsMessengerEXT");
        
        // Create and attach handler
        final PointerByReference ref = new PointerByReference();
        final Object[] args = {handle, info, null, ref};
        check(create.invokeInt(args));
        
        // Register handler
        handlers.add(ref.getValue());
    }
}
```

Notes:

* The JNA `invokeInt` method calls a function pointer with an arbitrary array of arguments.

* The name of the function is specified in the [Vulkan documentation](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#vkCreateDebugUtilsMessengerEXT).

* We maintain a list of the attached `handlers` in the instance (which will be released when we destroy the instance below).

Finally we add another helper to the instance to lookup a function pointer by name:

```java
/**
 * Looks up a Vulkan function by name.
 * @param name Function name
 * @return Vulkan function
 * @throws RuntimeException if the function cannot be found
 */
public Function function(String name) {
    final Pointer ptr = lib.vkGetInstanceProcAddr(handle, name);
    if(ptr == null) throw new RuntimeException("Cannot find function pointer: " + name);
    return Function.getFunction(ptr);
}
```

And we add the corresponding API method to the library:

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

### Message Callback

Next we implement the message callback invoked by Vulkan to report messages (which is a JNA callback):

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
     * @param pUserData         Optional user data (generally redundant for an OO approach)
     * @return Whether to continue execution (always {@code false})
     */
    public boolean message(int severity, int type, VkDebugUtilsMessengerCallbackData pCallbackData, Pointer pUserData) {
        // Transform bit-masks to enumerations
        final VkDebugUtilsMessageSeverity severityEnum = IntegerEnumeration.map(VkDebugUtilsMessageSeverity.class, severity);
        final Collection<VkDebugUtilsMessageType> typesEnum = IntegerEnumeration.enumerate(VkDebugUtilsMessageType.class, type);

        // Create message wrapper
        final Message message = new Message(severityEnum, typesEnum, pCallbackData);

        // Delegate to handler
        consumer.accept(message);

        // Continue execution
        return false;
    }
}
```

This code transforms the `severity` and message `types` bit-fields to the associated enumerations, constructs a `Message` record that wraps the message data, and delegates this object to the `consumer`.

Notes:

* The signature of the callback method is derived from the documentation (as an extension it is not part of the API).

* A JNA callback as an interface that must contain a __single__ public method, but this is not enforced at compile-time.

* The `pUserData` parameter is optional user data returned to the callback to correlate state, this is largely redundant for an OO implementation and is always `null` in our implementation.

We also add a custom `toString` implementation to the `Message` record to build a human-readable representation of a diagnostics report:

```java
@Override
public String toString() {
    final String compoundTypes = types.stream().map(Enum::name).collect(joining("-"));
    final StringJoiner str = new StringJoiner(":");
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

### Cleanup and Integration

To attach a diagnostics handler we must first enable the extension and register the standard validation layer when creating the instance:

```java
final Instance instance = new Instance.Builder()
        .name("InstanceDemo")
        .extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)     // <-- enable diagnostics extensions
        .extensions(desktop.extensions())
        .layer(ValidationLayer.STANDARD_VALIDATION)         // <-- enable validation layer
        .build(lib);
```

The diagnostics extension is defined in the main library:

```java
public interface VulkanLibrary {
    String EXTENSION_DEBUG_UTILS = "VK_EXT_debug_utils";
}
```

Finally we modify the `destroy` method of the instance to properly cleanup after ourselves and release attached message handlers:

```java
public void destroy() {
    if(!handlers.isEmpty()) {
        final Function destroy = function("vkDestroyDebugUtilsMessengerEXT");
        for(Pointer p : handlers) {
            final Object[] args = {handle, p, null};
            destroy.invoke(args);
        }
    }

    lib.vkDestroyInstance(handle, null);
}
```

Note that this implementation assumes that handlers persist for the lifetime of the instance (which seems a safe assumption).

In the demo application we can now create and attach a handler with a default configuration:

```java
new Handler()
        .severity(VkDebugUtilsMessageSeverity.WARNING)
        .severity(VkDebugUtilsMessageSeverity.ERROR)
        .type(VkDebugUtilsMessageType.GENERAL)
        .type(VkDebugUtilsMessageType.VALIDATION)
        .attach(instance);
```

From now on when we screw things up we should receive error messages on the console.

---

## Improvements

### Lazy Initialisation

There will be several cases throughout the Vulkan API where we would like to employ _lazy initialisation_ to defer the creation of some object and/or invocation of an API method.

For this we implement the [LazySupplier](TODO) which provides a relatively cheap, thread-safe means of lazily initialising some value.

We can now refactor the code that looks up the message handler function pointer so that it is only executed once:

```java
public class Instance extends AbstractTransientNativeObject {
    private final Supplier<Function> create = new LazySupplier<>(() -> function("vkCreateDebugUtilsMessengerEXT"));

    ...
    
    private void attach(VkDebugUtilsMessengerCreateInfoEXT info) {
        final PointerByReference handle = lib.factory().pointer();
        final Object[] args = {ptr, info, null, handle};
        check(create.get().invokeInt(args));                    // <-- Lazy initialisation happens here
        handlers.add(handle.getValue());
    }
}
```

### Aggregated API

Eventually there will be a large number of API methods (over a hundred) so we group logically related methods into separate interfaces and aggregate the overall library, for example:

```java
interface VulkanLibraryInstance {
    int vkCreateInstance(VkInstanceCreateInfo info, Handle allocator, PointerByReference instance);
    ...
}

interface VulkanLibrary extends Library, VulkanLibraryInstance, ... {
    static VulkanLibrary create() { ... }
}
```

This allows us to group logically related methods into a single aggregated API without creating an interface with hundreds of members.

---

## Testing Issues

### Background

The Vulkan API (and most other native libraries) make extensive use of by-reference types to return data, with the actual return value of the method generally representing some sort of error code (since base C does not support the notion of exceptions).

For example the `vkCreateInstance` API method returns the `handle` of the newly created instance via a JNA `PointerByReference` argument as shown here:

```java
public Instance build(VulkanLibrary lib) {
    ...
    
    // Create instance
    final PointerByReference handle = new PointerByReference();
    check(api.vkCreateInstance(info, null, handle));
    
    // Create instance wrapper
    return new Instance(api, handle.getValue());
}
```

Mercifully this approach is generally unknown in Java but it does pose an awkward problem when we come to testing - the usual Java unit-testing frameworks (JUnit, Mockito) are designed around Java APIs where the return value of a method is generally the important part with error conditions modelled by exceptions.

If we want to unit-test the above code we might try something like the following naive implementation:

```java
private VulkanLibrary lib;

@BeforeEach
void before() {
    lib = mock(VulkanLibrary.class);
}

@Test
void testBuild() {
    // Init expected create descriptor
    var info = new VkInstanceCreateInfo();
    ...

    // Init create method
    var handle = new PointerByReference();
    when(lib.vkCreateInstance(info, null, handle)).thenReturn(0);

    // Build instance
    Instance instance = new Instance.Builder()
        .name("testBuild")
        .extension("testException")
        .build(lib);

    // Check created instance        
    assertNotNull(instance);
    assertEquals(handle.getValue(), instance.handle());
    ...
}
```

However there are several immediate problems with this test.

### Problems

1. Verifying the API

The test is designed to check that the code invokes the correct API method with the expected arguments.  However by default _all_ methods in the mocked library will return zero (which of course is the Vulkan success return code) so the test passes whether we include the `when` pre-condition or not!

We could add a setup method that over-rides this behaviour by returning an error code by default:

```java
@BeforeEach
void before() {
    when(lib.vkCreateInstance(isA(VkInstanceCreateInfo.class), isNull(), isA(PointerByReference.class))).thenReturn(-1);
    ...
}
```

But this is ugly and error-prone, a better approach would be to avoid this 'catch-all' and instead add a post-condition that validates the expected method invocation:

```java
verify(lib).vkCreateInstance(info, null, handle);
```

2. Mocking by-reference parameters

But what do we supply for the `handle` argument?  We cannot simply pass a `PointerByReference` created in the test as it will be a different instance to the one created in the `build` method itself - the test would likely fail, or would only pass by 'luck'.

We could change the `verify` statement to use Mockito matchers and check that at least _something_ was passed for the handle argument:

```java
verify(lib).vkCreateInstance(..., isA(PointerByReference.class));
```

This means we cannot validate what was actually passed, only that is wasn't `null`.  Additionally __every__ argument would have to be an argument matcher which just adds more development complexity and obfuscates the code.

We could use a Mockito _answer_ for the handle to verify the actual returned value, but that would require tedious and convoluted code for _every_ unit-test that exercises methods with by-reference return values (which is pretty much all of them).

3. JNA structure equality

We also want to validate that the code constructs the expected `VkInstanceCreateInfo` descriptor, unfortunately it turns out that two JNA structures that contain the same data are __not__ equal and the above `verify` test would fail (even if we resolved the by-reference issue).  A review of the JNA source code shows that structures essentially violate the Java `equals` contract assumed by the testing frameworks, instead two structures can only be compared using the `dataEquals` method.

Again we could get around this by bastardising a custom structure implementation that 'fixes' the equality problem or by creating a custom Mockito argument matcher based on `dataEquals`, but either of these approaches would be fiddly, error prone and tediously repetitive to use.

Alternatively we could use a Mockito _argument captor_ which allows us to query the actual argument that was passed to the method (we could use the same approach for the handle reference):

```java
// Check API invocation
ArgumentCaptor<VkInstanceCreateInfo> captor = ArgumentCaptor.forClass(VkInstanceCreateInfo.class);
verify(lib).vkCreateInstance(captor.capture(), isNull(), isA(PointerByReference.class));

// Check create descriptor
VkInstanceCreateInfo info = captor.getValue();
assertTrue(expected.dataEquals(info));  // or validate individual structure fields
```

This is a lot of unpleasant code just to get around the fact that JNA structures do not support equality as expected.

### Mitigation

There are several inter-related issues here, we have some suggested workarounds that we _could_ use but none of them are particularly palatable.
We have yet to come up with a satisfying viable solution, however we have managed to mitigate some of the problems.

TODO
TODO
TODO

reference factory, show usage but not implementation, except for default in VK lib
should we just take the hit and use matchers, with custom dataEquals()?

In general from now on we will not cover testing unless there is a specific point-of-interest.  It can be assumed that unit-tests are developed in-parallel with the main code.

---

## Summary

In this first chapter we:

- Instantiated the Vulkan API and GLFW library.

- Created a Vulkan instance with the required extensions and layers.

- Attached a diagnostics handler.

