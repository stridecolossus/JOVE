---
title: The Vulkan Instance
---

## Overview

The first step in the development of JOVE is to create the Vulkan _instance_ - the starting point for everything that follows.

Creating the instance involves the following steps:

1. Instantiate the Vulkan API using JNA.

2. Specify the API extensions we require for our application.

3. Population of a JNA structure descriptor outlining our requirements for the instance.

4. Invoking the API to create the instance given this descriptor.

For this we will require the following components:

* Implementation of a JNA library to create/destroy the instance.

* An instance domain object.

* The relevant code generated structures used to specify the requirements of the instance.

As already mentioned in the [code generation](/JOVE/blog/part-1-generation/code-generation) chapter we will employ the GLFW library which provides services for managing windows, input devices, etc. that we will use in future chapters.  However another compelling reason to use GLFW is that it also offers functionality to create a Vulkan rendering surface suitable for the platform on which the application is executed.  We _could_ use Vulkan extensions to do this from the ground up but it makes sense to take advantage of the platform-independant implementation making our code considerably simpler.  The disadvantage of this approach is that the logic becomes a little convoluted as the surface and Vulkan components are slightly inter-dependant, but this seems an acceptable compromise.

To create an instance for a given platform we therefore also require:

* A second JNA library for GLFW.

* Additional domain objects for extensions and validation layers.

Finally we will also implement the diagnostics extension to support logging and error reporting which will become _very_ helpful in subsequent chapters.

---

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

* As detailed in the previous chapter we have intentionally decided to hand-craft the native methods rather than attempt to code-generate the API.

* Note that the handle to the newly created instance is returned as a `PointerByReference` object which maps to a native `VkInstance*` return-by-reference type.

* The `allocator` parameter in the two methods is out-of-scope for our library and is always set to `null`.

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
        api.vkDestroyInstance(handle, null);
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

The `Version` number is a simple Java object:

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

Finally we invoke the API method to create the instance given the descriptor:

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

The `VulkanException` is a custom exception class that maps a Vulkan return code to the corresponding `VkResult` to build an informative error message.





### Extensions and Validation Layers

There are two other pieces of information that we supply when creating the instance: extensions and validation layers.

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

The relevant fields of the instance descriptor are populated in the build method (which were left as defaults in the previous iteration):

```java
// Populate required extensions
info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
info.enabledExtensionCount = extensions.size();

// Populate required layers
info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
info.enabledLayerCount = layers.size();
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

Generally we will need to enable platform-specific extensions for the target platform so that we can actually perform rendering.

Usually there will be two extensions for a Vulkan rendering surface:
- the general surface: `VK_KHR_surface` 
- and the platform specific implementation, e.g. `VK_KHR_win32_surface` for Windows.

For this we will integrate the GLFW library that provides the following functionality relating to Vulkan:
- Test whether the current hardware supports Vulkan.
- Retrieve the platform-specific extensions required for Vulkan rendering.
- Create a Vulkan rendering surface for a given window.

We _could_ use the platform-specific extensions available in the Vulkan API but this approach is considerably simpler (since GLFW does the work for us) and we are planning to use this library for window and input device management anyway.

We create a new package called _desktop_ and define a new JNA library:

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
     * @return Whether vulkan is supported on this platform
     */
    boolean glfwVulkanSupported();

    /**
     * Enumerates the required vulkan extensions for this platform.
     * @param count Number of results
     * @return Vulkan extensions (pointer to array of strings)
     */
    Pointer glfwGetRequiredInstanceExtensions(IntByReference count);
}
```

Next we create the _desktop_ service:

```java
public class Desktop {
    public static Desktop create() {
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

Finally we can start work on our first demo application:

```java
public class TriangleDemo {
    public static void main(String[] args) throws Exception {
        // Init GLFW
        final Desktop desktop = Desktop.create();
        if(!desktop.isVulkanSupported()) throw new RuntimeException(...);
        
        // Init Vulkan
        final VulkanLibrary lib = VulkanLibrary.create();
        
        // Create instance
        final Instance instance = new Instance.Builder()
            .vulkan(lib)
            .name("test")
            .extensions(desktop.extensions())
            .layer(ValidationLayer.STANDARD_VALIDATION)
            .build();
                
        // Cleanup
        instance.destroy();
        desktop.destroy();
    }
}
```

We also add a convenience method to the builder to add the array of extensions returned by GLFW.

---

## Diagnostics Handler

### Message Callback

Vulkan implements the `STANDARD_VALIDATION` layer that provides an excellent error and diagnostics reporting mechanism, offering comprehensive logging as well as identifying common problems such as orphaned object handles, invalid parameters, performance warnings, etc.  This functionality is not mandatory but its safe to say it is _highly_ recommended during development, so we will address it now before we go any further.

However there is one complication - for some reason the reporting mechanism is not a core part of the API but is itself an extension.

We start with a new domain class that defines a message handler and the JNA callback invoked by Vulkan to report errors and messages:

```java
public static class Handler {
    /**
     * A <i>message callback</i> is invoked by Vulkan to report errors, diagnostics, etc.
     */
    private static class MessageCallback implements Callback {
    }

    private final Consumer<Message> handler;
}
```

We have to define the signature of the callback ourselves based on the documentation, as an extension it is not defined in the API:

```java
public boolean message(int severity, int type, VkDebugUtilsMessengerCallbackDataEXT pCallbackData, Pointer pUserData) {
}
```

Where:
- _severity_ is a bit-mask of the severity level(s) of the message (warning, error, etc).
- _type_ is a bit-mask of the message categories (validation, performance, etc).
- _pCallbackData_ is a structure containing the message details.
- _pUserData_ is an optional, arbitrary pointer associated with the handler (redundant for an OO implementation).

Note that JNA requires a callback implementation to contain a single method (the name is arbitrary) but this is not enforced by the compiler (as a functional interface for example).

The callback implementation transforms the bit-masks to the relevant enumerations and wraps the message in the new `Message` object:

```java
public boolean message(int severity, int type, VkDebugUtilsMessengerCallbackData pCallbackData, Pointer pUserData) {
    // Transform bit-masks to enumerations
    final VkDebugUtilsMessageSeverity severityEnum = IntegerEnumeration.map(VkDebugUtilsMessageSeverity.class, severity);
    final Collection<VkDebugUtilsMessageType> typesEnum = IntegerEnumeration.enumerate(VkDebugUtilsMessageType.class, type);

    // Create message wrapper
    final Message message = new Message(severityEnum, typesEnum, pCallbackData);

    // Delegate to handler
    handler.accept(message);

    // Continue execution
    return false;
}
```

The `Message` class is a simple POJO that composes the message details:

```java
public record Message(
    VkDebugUtilsMessageSeverity severity,
    Collection<VkDebugUtilsMessage> types,
    VkDebugUtilsMessengerCallbackData data
)
```

### Message Handler

We next implement the handler class itself which is a builder used to configure the diagnostic requirements for an application:

```java
public static class Handler {
    private final Manager manager;
    private final Set<VkDebugUtilsMessageSeverity> severity = new HashSet<>();
    private final Set<VkDebugUtilsMessageType> types = new HashSet<>();
    private Consumer<Message> handler = ...

    private Handler(Manager manager) {
        this.manager = manager;
    }

    public Handler handler(Consumer<Message> handler) {
        this.handler = notNull(handler);
        return this;
    }

    public Handler severity(VkDebugUtilsMessageSeverity severity) {
        this.severity.add(notNull(severity));
        return this;
    }

    public Handler type(VkDebugUtilsMessageType type) {
        types.add(notNull(type));
        return this;
    }
}
```

We add the convenience `init` method (not shown) that initialises the handler to common settings - warning/errors for general and validation messages.

The `attach` method populates the creation descriptor and attaches the handler to the instance:

```java
public void attach() {
    // Create handler descriptor
    final var info = new VkDebugUtilsMessengerCreateInfo();
    info.messageSeverity = IntegerEnumeration.mask(severity);
    info.messageType = IntegerEnumeration.mask(types);
    info.pfnUserCallback = new MessageCallback(handler);
    info.pUserData = null;

    // Create handler
    manager.create(info);
}
```

The `Manager` encapsulates the logic for creating and managing message handlers:

```java
private class Manager {
    private final Pointer dev;
    private final Collection<Pointer> handlers = new ArrayList<>();
    private final Supplier<Function> create = new LazySupplier<>(() -> function("vkCreateDebugUtilsMessengerEXT"));

    private Manager(Pointer dev) {
        this.dev = dev;
    }

    private void create(VkDebugUtilsMessengerCreateInfo info) {
        ...
    }
}
```

The `LazySupplier` is covered at the end of the chapter.

As already noted the diagnostics mechanism is an extension and not part of the public Vulkan API. 
The methods to create and destroy a message handler are function pointers looked up using the following API method (assuming the relevant extension is present):

```java
interface VulkanLibraryInstance {
    /**
     * Looks up an instance function.
     * @param instance      Vulkan instance
     * @param name          Function name
     * @return Function pointer
     */
    Pointer vkGetInstanceProcAddr(Handle instance, String name);
}
```

We implement a helper method on the `Instance` class to lookup a JNA function by name:

```java
public Function function(String name) {
    final Pointer ptr = lib.vkGetInstanceProcAddr(handle, name);
    if(ptr == null) throw new RuntimeException("Cannot find function pointer: " + name);
    return Function.getFunction(ptr);
}
```

The `create` method invokes the JNA function with an array of the relevant arguments (which again is determined from the documentation):

```java
private void create(VkDebugUtilsMessengerCreateInfoEXT info) {
    // Create handler
    final PointerByReference handle = new PointerByReference();
    final Object[] args = {Instance.this.handle, info, null, handle};
    check(create.get().invokeInt(args));

    // Register handler
    handlers.add(handle.getValue());
}
```

All this code is tied together in the `handler` factory method that creates a new message handler:

```java
public class Instance {
    private final VulkanLibrary lib;
    private final LazySupplier<Manager> manager;

    private Instance(VulkanLibrary lib, Pointer handle) {
        super(handle);
        this.manager = new LazySupplier<>(() -> new Manager(handle));
        this.lib = notNull(lib);
    }

    /**
     * Creates a builder for a new message handler to be attached to this instance.
     * @return New message handler builder
     */
    public Handler handler() {
        return new Handler(manager.get());
    }
}
```

Finally we add the following method to the manager to release attached handlers:

```java
private void destroy() {
    // Ignore if unused
    if(handlers.isEmpty()) {
        return;
    }

    // Lookup destroy API method
    final Function destroy = function("vkDestroyDebugUtilsMessengerEXT");

    // Release handlers
    for(Pointer handle : handlers) {
        final Object[] args = new Object[]{dev, handle, null};
        destroy.invoke(args);
    }
}
```

And the destructor of the instance is modified accordingly:

```java
@Override
public void destroy() {
    manager.get().destroy();
    lib.vkDestroyInstance(handle, null);
}
```

Note that this implementation assumes that handlers persist for the lifetime of the instance (which seems a safe assumption).

### Message Formatting

With the framework in place we can now add formatting functionality to the `Message` class.

The message is formatted to a colon-delimited string via its `toString` implementation:

```java
@Override
public String toString() {
    final String compoundTypes = types.stream().map(Message::toString).collect(joining("-"));
    final StringJoiner str = new StringJoiner(":");
    str.add(toString(severity));
    str.add(compoundTypes);
    if(!data.pMessage.contains(data.pMessageIdName)) {
        str.add(data.pMessageIdName);
    }
    str.add(data.pMessage);
    return str.toString();
}
```

Which uses the following helpers to convert the enumeration values to human-readable tokens:

```java
public static String toString(VkDebugUtilsMessageSeverity severity) {
    return clean(severity.name(), "SEVERITY");
}

public static String toString(VkDebugUtilsMessageType type) {
    return clean(type.name(), "TYPE");
}

private static String clean(String name, String type) {
    final String prefix = new StringBuilder()
            .append("VK_DEBUG_UTILS_MESSAGE_")
            .append(type)
            .append("_")
            .toString();

    return removeEnd(removeStart(name, prefix), "_BIT_EXT");
}
```

The `clean` method is possibly slightly confusing but it basically just strips the prefix and suffix of an enumeration constant,
for example `SEVERITY_VERBOSE` becomes `VERBOSE`.

Example formatted message (excluding the message text):

```
ERROR:VALIDATION:Validation Error: [ VUID-VkDeviceQueueCreateInfo-pQueuePriorities-00383 ] ...
```

Finally we add the following consumer which dumps messages to the console and is used as the default in the builder:

```java
/**
 * Message handler that outputs to the console.
 */
public static final Consumer<Message> CONSOLE = System.err::println;
```

### Integration

To attach a diagnostics handler we must first enable the extension:

```java
final Instance instance = new Instance.Builder()
    ...
    .extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
    .build();
```

Which is defined in the Vulkan API:

```java
public interface VulkanLibrary {
    /**
     * Debug utility extension.
     */
    String EXTENSION_DEBUG_UTILS = "VK_EXT_debug_utils";
}
```

Finally in the demo we create and attach a handler with a default configuration:

```java
instance
    .handler()
    .init()
    .attach();
```

From now on when we screw things up we should receive error messages on the console.

---

## Improvements

### Lazy Initialisation

There will several cases throughout the Vulkan API where we will employ _lazy initialisation_ to defer instantiation of some object and/or invocation of an API method.

We create a custom supplier for lazy initialisation:

```java
public class LazySupplier<T> implements Supplier<T> {
    private final Supplier<T> supplier;

    private volatile T value;

    public LazySupplier(Supplier<T> supplier) {
        this.supplier = notNull(supplier);
    }

    @Override
    public T get() {
        final T result = value;

        if(result == null) {
            synchronized(this) {
                if(value == null) {
                    value = notNull(supplier.get());
                }
                return value;
            }
        }
        else {
            return result;
        }
    }
}
```

The rationale for lazy initialisation is not performance reasons but mainly to ensure that API methods are invoked closer to the code where they are used
(such as when we lookup the create method for the diagnostics handler). 

However this implementation is also relatively cheaply thread-safe should that become a requirement:

The first line of the `get` method may look odd or even pointless:

```java
final T result = value;
```

This is performing **one** read of the _volatile_ lazily instantiated object allowing the following code to avoid the `synchronized` block if the value has been populated.

Reference: [DZone article](https://dzone.com/articles/be-lazy-with-java-8)

### Reference Factory

TODO

The Vulkan API (and many other native libraries) make extensive use of by-reference types to return data (with the return value generally being some sort of error code).
For example the `vkCreateInstance` API method returns the handle of the newly instance created instance via a JNA `PointerByReference` from which the actual handle is extracted.

Mercifully this approach is generally uncommon in Java but it poses an awkward problem for unit-testing - if the code allocates the by-reference internally we have no way of mocking it beforehand, the returned pointer will be un-initialised and subsequent code that depends on that value will fail.

We _could_ mock the reference using a Mockito _answer_ but that means tedious and convoluted code that we have to implement for _every_ unit-test that exercises a by-reference value.

Therefore we factor out the most common cases into a _reference factory_ helper that is responsible for creating any by-reference types in the API that we can then more easily mock.
The factory is defined as follows with a concrete implementation for production code:

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

    /**
     * Default implementation.
     */
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

The factory is a property of the Vulkan API:

```java
public interface VulkanLibrary {
    /**
     * @return Factory for pass-by-reference types used by this API
     * @see ReferenceFactory#DEFAULT
     */
    default ReferenceFactory factory() {
        return ReferenceFactory.DEFAULT;
    }
}
```

Notes:

- We provide a convenience `MockReferenceFactory` implementation to support testing.

- The use of a default method in the Vulkan API is a little dodgy but it means the production implementation is returned by default.

The factory allows us to mock the creation of any by-reference values as required, for example:

```java
// Init API
lib = mock(VulkanLibrary.class);
when(lib.factory()).thenReturn(new MockReferenceFactory());

// Create instance
verify(lib).vkCreateInstance(...);

// Check handle
final PointerByReference handle = lib.factory().pointer();
assertEquals(handle.getValue(), instance.handle());
```

In general from now on we will not cover testing unless there is a specific point-of-interest and it can be assumed that tests are developed in-parallel with the main code.

---

## Summary

In this first chapter we:

- Instantiated the Vulkan API.

- Created the instance with the required extensions and layers.

- And attached a diagnostics handler.




Eventually there will be a large number of API methods (over a hundred) so we group logically related methods into their own interface and aggregate into the overall library:

```java
interface VulkanLibrary extends Library, VulkanLibraryInstance, ... {
}
```



