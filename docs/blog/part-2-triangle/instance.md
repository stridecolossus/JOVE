---
title: The Vulkan Instance
---

## Overview

The first step in the development of our library is to create a Vulkan _instance_, the starting point for everything that follows.

This involves:
1. Instantiating the Vulkan API using JNA
2. Specifying any extensions we require for our application
3. Populating a JNA structure descriptor for the instance
4. Creating the instance given this descriptor

We will also enable a diagnostics extension to support logging and error reporting.

So let's get cracking.

---

## Creating the Vulkan Instance

### Instance API

We start with an empty interface for the Vulkan API instantiated by JNA via a static factory method:

```java
interface VulkanLibrary extends Library {
    static VulkanLibrary create() {
        final String name = switch(Platform.getOSType()) {
            case Platform.WINDOWS -> "vulkan-1";
            case Platform.LINUX -> "libvulkan";
            default -> throw new UnsupportedOperationException("Unsupported platform: " + Platform.getOSType());
        }

        return Native.load(name, VulkanLibrary.class);
    }
}
```

Next we extend the API by defining the methods to create and destroy an instance:

```java
interface VulkanLibraryInstance {
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

This interface maps to the following methods defined in the `vulkan_core.h` header:

```java
typedef VkResult (VKAPI_PTR *PFN_vkCreateInstance)(const VkInstanceCreateInfo* pCreateInfo, const VkAllocationCallbacks* pAllocator, VkInstance* pInstance);
typedef void (VKAPI_PTR *PFN_vkDestroyInstance)(VkInstance instance, const VkAllocationCallbacks* pAllocator);
```

Eventually there will be a large number of API methods (over a hundred) so we group logically related methods into their own interface and aggregate into the overall library:

```java
interface VulkanLibrary extends Library, VulkanLibraryInstance, ... {
}
```

As noted in the chapter on [code generation](/JOVE/blog/part-1-generation/code-generation) we have intentionally decided not to code-generate the API.

### Vulkan Instance

With that in place we can now create our first domain class:

```java
public class Instance {
    private final VulkanLibrary api;
    private final Pointer handle;

    private Instance(VulkanLibrary api, Pointer handle) {
        this.api = notNull(api);
        this.handle = notNull(handle);
    }

    VulkanLibrary api() {
        return api;
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
    private VulkanLibrary lib;
    private String name;
    private Version ver = new Version(1, 0, 0);

    public Builder vulkan(VulkanLibrary api) {
        this.lib = notNull(api);
        return this;
    }

    /**
     * Sets the application name.
     * @param name Application name
     */
    public Builder name(String name) {
        this.name = notEmpty(name);
        return this;
    }

    /**
     * Sets the application version.
     * @param ver Application version
     */
    public Builder version(Version ver) {
        this.ver = notNull(ver);
        return this;
    }

    public Instance build() {
    }
}
```

The `Version` number is a simple POJO:

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

In the `build()` method we use the code-generated structures for the first time to populate the application and engine details:

```java
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

Next we populate the descriptor for the instance (just the application details for the moment):

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
```

Note that the handle to the newly created instance is returned in the `PointerByReference` object which maps to a native `VkInstance*` return-by-reference type.

The `check()` method wraps an API call and validates the result code:

```java
public interface VulkanLibrary {
    /**
     * Successful result code.
     */
    int SUCCESS = VkResult.VK_SUCCESS.value();
    
    /**
     * Checks the result of a Vulkan operation.
     * @param result Result code
     * @throws VulkanException if the given result is not {@link VkResult#VK_SUCCESS}
     */
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

We start with a new domain class that defines a handler and the JNA callback invoked by Vulkan to report errors and messages:

```java
public static class Handler {
    /**
     * A <i>message callback</i> is invoked by Vulkan to report errors, diagnostics, etc.
     */
    private static class MessageCallback implements Callback {
    }
}
```

We have to define the signature of the callback ourselves using the documentation, as an extension it is not defined in the API:

```java
public boolean message(int severity, int type, VkDebugUtilsMessengerCallbackDataEXT pCallbackData, Pointer pUserData) {
}
```

Where:
- _severity_ is a bit-mask of the severity level(s) of the message (warning, error, etc).
- _type_ is a bit-mask of the message categories (validation, performance, etc).
- _pCallbackData_ is a structure containing the message details.
- _pUserData_ is an optional, arbitrary pointer associated with the handler (redundant for an OO implementation).

The callback implementation transforms the bit-masks to the relevant enumerations and wraps the message in a new domain object:

```java
public boolean message(int severity, int type, VkDebugUtilsMessengerCallbackDataEXT pCallbackData, Pointer pUserData) {
    // Transform bit-masks to enumerations
    final VkDebugUtilsMessageSeverityFlagEXT severityEnum = IntegerEnumeration.map(VkDebugUtilsMessageSeverityFlagEXT.class, severity);
    final Collection<VkDebugUtilsMessageTypeFlagEXT> typesEnum = IntegerEnumeration.enumerate(VkDebugUtilsMessageTypeFlagEXT.class, type);

    // Create message wrapper
    final Message message = new Message(severityEnum, typesEnum, pCallbackData);

    // Delegate to handler
    handler.accept(message);

    // Continue execution
    return false;
}
```

The `Message` class is a simple POJO:

```java
public record Message(
    VkDebugUtilsMessageSeverityFlagEXT severity,
    Collection<VkDebugUtilsMessageTypeFlagEXT> types,
    VkDebugUtilsMessengerCallbackDataEXT data
)
```

Which is passed to a simple handler:

```java
private static class MessageCallback implements Callback {
    private final Consumer<Message> handler;

    private MessageCallback(Consumer<Message> handler) {
        this.handler = notNull(handler);
    }
}
```

### Message Handler

We next implement the handler class itself which is a builder used to specify the diagnostic requirements for an application:

```java
public static class Handler {
    private final Manager manager;
    private final Set<VkDebugUtilsMessageSeverityFlagEXT> severity = new HashSet<>();
    private final Set<VkDebugUtilsMessageTypeFlagEXT> types = new HashSet<>();
    private Consumer<Message> handler = ...

    private Handler(Manager manager) {
        this.manager = manager;
    }

    public Handler handler(Consumer<Message> handler) {
        this.handler = notNull(handler);
        return this;
    }

    public Handler severity(VkDebugUtilsMessageSeverityFlagEXT severity) {
        this.severity.add(notNull(severity));
        return this;
    }

    public Handler type(VkDebugUtilsMessageTypeFlagEXT type) {
        types.add(notNull(type));
        return this;
    }
}
```

The convenience `init` method (not shown) initialises the handler to common settings - warning/errors for general and validation messages.

The 'build' method of the handler populates a descriptor and attaches the handler to the instance:

```java
public void attach() {
    // Create handler descriptor
    final VkDebugUtilsMessengerCreateInfoEXT info = new VkDebugUtilsMessengerCreateInfoEXT();
    info.messageSeverity = IntegerEnumeration.mask(severity);
    info.messageType = IntegerEnumeration.mask(types);
    info.pfnUserCallback = new MessageCallback(handler);
    info.pUserData = null;

    // Create handler
    manager.create(info);
}
```

The `manager` encapsulates the logic for creating and managing the message handlers:

```java
private class Manager {
    private final Pointer dev;
    private final Collection<Pointer> handlers = new ArrayList<>();
    private final Supplier<Function> create = new LazySupplier<>(() -> function("vkCreateDebugUtilsMessengerEXT"));

    private Manager(Pointer dev) {
        this.dev = dev;
    }

    private void create(VkDebugUtilsMessengerCreateInfoEXT info) {
        ...
    }
}
```

The `LazySupplier' is covered at the end of the chapter.

As already noted the diagnostics mechanism is an extension and not part of the public Vulkan API, therefore we add the following helper to the `Instance` to lookup the API method to create the handler:

```java
public Function function(String name) {
    final Pointer ptr = lib.vkGetInstanceProcAddr(handle, name);
    if(ptr == null) throw new RuntimeException("Cannot find function pointer: " + name);
    return Function.getFunction(ptr);
}
```

The `create` method invokes this function with an array of the relevant arguments (which again we have to determine from the documentation):

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

All this code is tied together in the following factory method on the instance class:

```java
public class Instance {
    private final VulkanLibrary lib;
    private final LazySupplier<Manager> manager;

    /**
     * Creates a builder for a new message handler to be attached to this instance.
     * @return New message handler builder
     */
    public Handler handler() {
        return new Handler(manager.get());
    }
}
```

Finally we add the following method to the manager to release all attached handlers:

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

Note that this implementation assumes that handlers persist for the lifetime of the instance (which seems a safe assumption at this stage).

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
public static String toString(VkDebugUtilsMessageSeverityFlagEXT severity) {
    return clean(severity.name(), "SEVERITY");
}

public static String toString(VkDebugUtilsMessageTypeFlagEXT type) {
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
for example `VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT` becomes `VERBOSE`.

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

Finally we create and attach a handler with the default configuration in the demo:

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

We implement a custom supplier for handle lazy instantiation of some object:

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

We implement the `LazySupplier` not for performance reasons but mainly to ensure that API methods are invoked closer to the code where they are used
(such as when we lookup the create method for the diagnostics handler). 
However this implementation is also thread-safe should that become a requirement.

The first line of the `get` method may look odd or even pointless:

```java
final T result = value;
```

This is performing **one** read of the _volatile_ lazily instantiated object which allows the following code to avoid the `synchronized` block if the value has been populated.

Reference: [DZone article](https://dzone.com/articles/be-lazy-with-java-8)

### Reference Factory

TODO

The Vulkan API (and many other native libraries) make extensive use of by-reference types to return data (with the return value generally being some sort of error code).
For example the `vkCreateInstance()` API method returns the handle of the newly instance created instance via a JNA `PointerByReference` from which the actual handle is extracted.

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

