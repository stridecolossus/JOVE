# Overview

The first step in the development of our library is to create a Vulkan _instance_ which is the starting point for everything that follows.

This involves:
1. Instantiating the Vulkan API using JNA
2. Specifying any extensions we require for our application
3. Populating a JNA structure descriptor for the instance
4. Creating the instance given this descriptor

We will also enable the diagnostics extension to support logging and error reporting.

So let's get cracking.


# The Vulkan API

We start with an empty interface for the Vulkan API that is instantiated using JNA via a static factory method:

```java
interface VulkanLibrary extends Library {
    static VulkanLibrary create() {
        return Native.load(library(), VulkanLibrary.class);
    }
}
```

We add a private helper that determines the name of the native library for our platform:

```java
private static String library() {
    return switch(Platform.getOSType()) {
        case Platform.WINDOWS -> "vulkan-1";
        case Platform.LINUX -> "libvulkan";
        default -> throw new UnsupportedOperationException("Unsupported platform: " + Platform.getOSType());
    }
}
```


# Creating the Vulkan Instance

## Instance API

Next we extend the API by adding the methods to create and destroy an instance:

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

As noted in the chapter on [code generation](https://stridecolossus.blogspot.com/2020/10/part-1-generation-game.html) we have intentionally decided not to code-generate the API.

There will eventually be a large number of API methods (over a hundred) so we group logically related methods into a single interface which can then be aggregated:

```java
interface VulkanLibrary extends Library, VulkanLibraryInstance {
}
```

## Vulkan Instance

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

The application version number is a simple POJO:

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

The build method uses the code-generated structures for the first time:

```java
// Init application descriptor
final VkApplicationInfo app = new VkApplicationInfo();
app.pApplicationName = name;
app.applicationVersion = ver.toInteger();
app.pEngineName = "JOVE";
app.engineVersion = new Version(1, 0, 0).toInteger();
app.apiVersion = VulkanLibrary.VERSION.toInteger();

// Init instance descriptor
final VkInstanceCreateInfo info = new VkInstanceCreateInfo();
info.pApplicationInfo = app;
```

The `apiVersion` field is the **maximum** version of the Vulkan API that the application can use - we add a new constant to the API for the version supported by the SDK:

```java
public interface VulkanLibrary {
    Version VERSION = new Version(1, 1, 0);
}
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

The `check()` method wraps the API call and validates the result code:

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



# Extensions and Validation Layers

## Extending the Builder

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

Note the use of `StringArray` which is a JNA helper that maps a Java array-of-strings to a native `const char* const*` type.

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

## Required Extensions

Generally we will need to enable platform-specific extensions for the target platform so that we can actually perform rendering.

For this we will integrate the GLFW library that provides the following functionality relating to Vulkan:
- Test whether the current hardware supports Vulkan.
- Retrieve the platform-specific extensions required for Vulkan rendering.
- Create a Vulkan rendering surface for a given window.

We _could_ use the platform-specific extensions available in the Vulkan API but this approach is considerably simpler (since GLFW does the work for us) and we are planning to use it for window  and input device management anyway.

We create a new package called _desktop_ and create a new JNA library:

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

Usually there will be two required extensions for the Vulkan rendering surface:
- the general surface: `VK_KHR_surface` 
- and one for the specific platform, e.g. `VK_KHR_win32_surface` for Windows.


# Integration and Testing

## Unit-Tests

The unit-test for the instance class mainly exercises the builder since our domain object has little functionality at the moment:

```java
public class InstanceTest {
    private VulkanLibrary lib;
    private Instance instance;

    @BeforeEach
    void before() {
        // Init API
        lib = mock(VulkanLibrary.class);

        // Create instance
        instance = new Instance.Builder()
                .vulkan(lib)
                .name("test")
                .extension("ext")
                .layer(new ValidationLayer("layer"))
                .build();
    }

    @Test
    void constructor() {
        assertNotNull(instance);
        assertEquals(lib, instance.library());
        assertNotNull(instance.handle());
    }

    @Test
    void create() {
        // Check API invocation
        final PointerByReference ref = ... // TODO
        final ArgumentCaptor<VkInstanceCreateInfo> captor = ArgumentCaptor.forClass(VkInstanceCreateInfo.class);
        verify(lib).vkCreateInstance(captor.capture(), isNull(), eq(ref));

        // Check instance descriptor
        final VkInstanceCreateInfo info = captor.getValue();
        assertEquals(1, info.enabledExtensionCount);
        assertEquals(1, info.enabledLayerCount);
        assertNotNull(info.ppEnabledExtensionNames);
        assertNotNull(info.ppEnabledLayerNames);

        // Check application descriptor
        final VkApplicationInfo app = info.pApplicationInfo;
        assertNotNull(app);
        assertEquals("test", app.pApplicationName);
        assertNotNull(app.applicationVersion);
        assertEquals("JOVE", app.pEngineName);
        assertNotNull(app.engineVersion);
        assertEquals(VulkanLibrary.VERSION.toInteger(), app.apiVersion);
    }

    @Test
    void destroy() {
        instance.destroy();
        verify(lib).vkDestroyInstance(instance.handle(), null);
    }
}
```

The reason for presenting this unit-test is to highlight the `PointerByReference` in the `create()` test which we discuss in the following section.

## Reference Factory

The Vulkan API (and many other native libraries) make extensive use of by-reference types to return data (with the return value generally being some sort of error code).
The `vkCreateInstance()` API method returns the handle of the newly instance created instance via a JNA `PointerByReference` from which the actual handle is extracted.

Mercifully this approach is generally uncommon in Java but it poses an awkward problem for unit-testing - if the code allocates the by-reference internally we have no way of mocking it beforehand, the returned pointer will be initialised to zero, and subsequent code that depends on that value will fail.

We _could_ mock the reference using a Mockito _answer_ but that means tedious and convoluted code that we have to implement for _every_ unit-test that exercises a by-reference value.

Therefore we factor out the most common cases into a _reference factory_ helper that is responsible for creating by-reference types in the API that we can then more easily mock.
The factory is defined as follows with a concrete implementation used for production code:

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
- We provide the a convenience `MockReferenceFactory` implementation that also initialises by-reference types to reduce the amount of boiler-plate required.
- The use of the default method in the Vulkan API is a little dodgy but it allows us to easily use this mock implementation.

This allows us to mock the creation of any by-reference values as required, for example:

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

## Integration

Finally we start work on our first demo application:

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

We add a convenience method to the builder to add the array of extensions returned by GLFW.



# Diagnostics Handler

## Overview

Vulkan implements the `STANDARD_VALIDATION` validation layer described above that provides a very comprehensive error and diagnostics reporting mechanism, offering useful logging as well as alerting common problems such as orphaned object handles, invalid parameters, performance warnings, etc.  This functionality is not mandatory but it is _highly_ recommended during development so we will address how it used now before we go any further.

However there is one complication - for some reason the reporting mechanism is not a core part of the API but is implemented as an extension.

## Message Handler

We start with a new domain class that will represent our reporting requirements and a JNA callback that is invoked by Vulkan to report errors and messages:

```java
public class MessageHandler {
    /**
     * A message handler <i>callback</i> is invoked by Vulkan to report errors, diagnostics, etc.
     */
    public interface MessageCallback extends Callback {
        /**
         * Invoked on receipt of a debug message.
         * @param severity            Severity
         * @param type                Message type(s)
         * @param pCallbackData        Message
         * @param pUserData            User data
         * @return Whether to terminate or continue
         */
        boolean message(int severity, int type, VkDebugUtilsMessengerCallbackDataEXT pCallbackData, Pointer pUserData);
    }

    private final MessageCallback callback;
    private final Pointer data;
    private final Set<VkDebugUtilsMessageSeverityFlagEXT> severities;
    private final Set<VkDebugUtilsMessageTypeFlagEXT> types;

    /**
     * Constructor.
     * @param callback            Callback handler
     * @param data                Optional user data
     * @param severities        Message severities
     * @param types                Message types
     */
    public MessageHandler(MessageCallback callback, Pointer data, Set<VkDebugUtilsMessageSeverityFlagEXT> severities, Set<VkDebugUtilsMessageTypeFlagEXT> types) {
        this.callback = notNull(callback);
        this.data = data;
        this.severities = Set.copyOf(notEmpty(severities));
        this.types = Set.copyOf(notEmpty(types));
    }
}
```

Notes:

- We have to define the callback ourselves - as an extension it is not defined in the API and we are just expected to know the signature.

- The _data_ field is a an arbitrary object associated with the handler returned in the callback (redundant for an OO implementation but we include it for completeness).

- The _severities_ field specifies the severity level(s) of the messages of interest (warning, error, etc)

- The _types_ field enumerates the message categories (validation, performance, etc). 

- We also add a convenience builder to specify a handler.

## Handler Manager

We will add a new lazily-instantiated local class to the instance that encapsulates the management of message handlers:

```java
public class Instance {
    private HandlerManager manager;

    public synchronized HandlerManager handlers() {
        if(manager == null) {
            manager = new HandlerManager();
        }

        return manager;
    }
}
```

The manager looks up the functions to create and destroy a handler from the extension:

```java
public class HandlerManager {
    private final Function create;
    private final Function destroy;

    private HandlerManager() {
        this.create = function("vkCreateDebugUtilsMessengerEXT");
        this.destroy = function("vkDestroyDebugUtilsMessengerEXT");
    }
}
```

We add the following helper to the instance class to lookup a JNA function by name:

```java
public Function function(String name) {
    final Pointer ptr = lib.vkGetInstanceProcAddr(handle, name);
    if(ptr == null) throw new RuntimeException("Cannot find function pointer: " + name);
    return Function.getFunction(ptr);
}
```

## Attaching a Handler

To attach a message handler we first add the following method to the handler to create the Vulkan descriptor:

```java
protected VkDebugUtilsMessengerCreateInfoEXT create() {
    final VkDebugUtilsMessengerCreateInfoEXT info = new VkDebugUtilsMessengerCreateInfoEXT();
    info.messageSeverity = IntegerEnumeration.mask(severities);
    info.messageType = IntegerEnumeration.mask(types);
    info.pfnUserCallback = callback;
    info.pUserData = data;
    return info;
}
```

Note the use of the `mask` method to transform the enumeration collections to a bit-field mask.

We then invoke the create function with an argument array containing the descriptor and the handle of the parent instance:

```java
public void add(MessageHandler handler) {
    // Check handler is valid
    Check.notNull(handler);
    if(handlers.containsKey(handler)) throw new IllegalArgumentException(...);

    // Create handler
    final VkDebugUtilsMessengerCreateInfoEXT info = handler.create();
    final PointerByReference handle = lib.factory().pointer();
    final Object[] args = {Instance.this.handle, info, null, handle};
    check(create.invokeInt(args));

    // Register handler
    handlers.put(handler, handle.getValue());
}
```

## Cleanup

We add the following methods to the handler manager to cleanup handlers:

```java
public void remove(MessageHandler handler) {
    if(!handlers.containsKey(handler)) throw new IllegalArgumentException("Handler not present: " + handler);
    final Pointer handle = handlers.remove(handler);
    destroy(handle);
}

private void destroy(Pointer handle) {
    final Object[] args = new Object[]{Instance.this.handle, handle, null};
    destroy.invoke(args);
}

private void destroy() {
    handlers.values().forEach(this::destroy);
    handlers.clear();
}
```

And the destructor of the instance is modified as follows:

```java
@Override
public synchronized void destroy() {
    // Destroy active handlers
    if(manager != null) {
        manager.destroy();
    }

    // Destroy instance
    lib.vkDestroyInstance(handle, null);
}
```

## Message Convenience

With the framework in place we add some additional helper functionality to the message handler:

- We add the convenience `init()` method to initialises the builder to a common configuration (errors and warnings only).

- Next we add the `AbstractMessageCallback` class which is a skeleton implementation that handles the mapping of the severity and type fields.

- We add a concrete implementation of this skeleton class created by the `writer()` factory method that formats and outputs a message to a writer.

- Finally we add the `CONSOLE` implementation that dumps the message to the console (which now becomes the default callback in the builder).

## Integration

To attach a diagnostics handler we must first enable the extension:

```java
final Instance instance = new Instance.Builder()
    .vulkan(lib)
    .name("test")
    .extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
    .extensions(desktop.extensions())
    .layer(ValidationLayer.STANDARD_VALIDATION)
    .build();
```

The extension is defined in the Vulkan API:

```java
public interface VulkanLibrary {
    /**
     * Debug utility extension.
     */
    String EXTENSION_DEBUG_UTILS = "VK_EXT_debug_utils";
}
```

Finally we create and attach the handler:

```java
final var handler = new MessageHandler.Builder()
    .init()
    .callback(MessageHandler.CONSOLE)
    .build();

instance.handlers().add(handler);
```

From now on when we screw things up we should receive error messages on the console.


# Summary

In this first chapter we instantiated the Vulkan API, created the instance object with the required extensions and layers, and attached a diagnostics handler.

