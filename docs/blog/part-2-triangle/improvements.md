---
title: Interlude - Some Improvements
---

## Overview

In this short interlude we collate a number of improvements to the code we have crafted thus far.

These are all framework improvements that we identified during development rather than changes to any specific Vulkan component.

---

## Handles

Up until now most of our Vulkan domain classes have a handle represented by a JNA pointer.

There are a couple of issues with this approach:

- a JNA pointer is mutable so if we expose the handle we potentially break the class.

- There are several API methods that require us to pass an array of pointers which currently requires fiddly transformations.

To solve the mutability issue and alleviate the second problem we introduce the following interface to define a native object that has a pointer handle:

```java
/**
 * A <i>native object</i> is a resource managed by a native library and referenced by a JNA pointer.
 */
public interface NativeObject {
    /**
     * @return Handle
     */
    Handle handle();

    /**
     * A <i>transient native object</i> can be destroyed by the application.
     */
    interface TransientNativeObject extends NativeObject {
        /**
         * Destroys this object.
         */
        void destroy();
    }

    /**
     * A <i>handle</i> is an opaque wrapper for a JNA pointer referencing a native object.
     */
    final class Handle {
        /**
         * Native type converter for a handle.
         */
        public static final TypeConverter CONVERTER = new TypeConverter() {
            ...
        };

        /**
         * Creates an array of handle pointers for the given native objects.
         * @param objects Native objects
         * @return Pointer array
         */
        public static Pointer[] toArray(Collection<? extends NativeObject> objects) {
            return objects
                .stream()
                .map(NativeObject::handle)
                .map(obj -> obj.handle)
                .toArray(Pointer[]::new);
        }

        private final Pointer handle;

        /**
         * Constructor.
         * @param handle Pointer handle
         */
        public Handle(Pointer handle) {
            Check.notNull(handle);
            this.handle = new Pointer(Pointer.nativeValue(handle));
        }

      ...
    }
}
```

This change:

- Makes a handle opaque and immutable.

- Provides a centralised helper to convert a collection of native objects to a pointer array.

- Makes API methods and structures slightly more type-safe.

- Allows us to refer to **all** native objects if (for example) we wanted to implement a resource tracker.

We refactor all the existing GLFW and Vulkan domain objects accordingly and add the type converter to the Vulkan API.

---

## Abstract Vulkan Object

With the logical device in place we will be implementing a number of new domain components that share the same requirements:

- A handle.

- A reference to the 'parent' logical device.

- A _destructor_ method, e.g. `vkDestroyShaderModule` to destroy a shader.

Although we generally avoid base-classes this seems a valid case to factor out the common pattern:

```java
public abstract class AbstractVulkanObject implements TransientNativeObject {
    /**
     * Destructor method.
     */
    @FunctionalInterface
    public interface Destructor {
        /**
         * Destroys this object.
         * @param dev           Logical device
         * @param handle        Handle
         * @param allocator     Allocator
         */
        void destroy(Handle dev, Handle handle, Handle allocator);
    }

    private final Handle handle;
    private final LogicalDevice dev;
    private final Destructor destructor;
    
    private boolean destroyed;

    /**
     * Constructor.
     * @param handle        Handle
     * @param dev           Parent logical device
     * @param destructor    Destructor API method
     */
    protected AbstractVulkanObject(Pointer handle, LogicalDevice dev, Destructor destructor) {
        this.handle = new Handle(handle);
        this.dev = notNull(dev);
        this.destructor = notNull(destructor);
    }

    @Override
    public Handle handle() {
        return handle;
    }

    /**
     * @return Parent logical device
     */
    public LogicalDevice device() {
        return dev;
    }

    /**
     * @return Whether this object has been destroyed
     */
    public boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public synchronized void destroy() {
        if(destroyed) throw new IllegalStateException("Object has already been destroyed: " + this);
        destructor.destroy(dev.handle(), handle, null);
        destroyed = true;
    }
}
```

A domain class that is dependent on the logical device can now be more easily implemented as a sub-class, e.g.

```java
public class Shader extends AbstractVulkanObject {
    private Shader(Pointer handle, LogicalDevice dev) {
        super(handle, dev, dev.library()::vkDestroyShaderModule);
    }
}
```

---

## Testing Support

Any unit-test that is dependant on the logical device (which is the majority of them) generally require the same test setup which we factor out to a test base-class:

```java
public abstract class AbstractVulkanTest {
    protected MockReferenceFactory factory;
    protected LogicalDevice dev;
    protected VulkanLibrary lib;

    @BeforeEach
    private final void beforeVulkanTest() {
        // Create API
        lib = mock(VulkanLibrary.class);

        // Init reference factory
        factory = new MockReferenceFactory();
        when(lib.factory()).thenReturn(factory);

        // Create logical device
        dev = mock(LogicalDevice.class);
        when(dev.handle()).thenReturn(new Handle(new Pointer(42)));
        when(dev.library()).thenReturn(lib);
    }
}
```

Note that we give the setup method a relatively unique name (and make it private and final) to avoid potentially conflicting with the sub-class.

---

## Two-Stage Invocation

In the [devices chapter](/JOVE/blog/part-2-triangle/devices) we first came across API methods that are invoked **twice** to retrieve an array from Vulkan.

For example when we query the extensions supported by the physical device we invoke the `vkEnumerateDeviceExtensionProperties` API method:

1. Once to determine the size of the array which is returned as an integer-by-reference (the array itself is `null`).

2. And again to populate the array (passing back the size and an empty array).

This is a common pattern across the Vulkan API which we refer to as _two-stage invocation_.

We define the following interface to abstract a Vulkan API method that utilises the two-stage invocation approach:

```java
public interface VulkanFunction<T> {
    /**
     * Vulkan API method that retrieves an array of the given type.
     * @param lib        Vulkan library
     * @param count     Return-by-reference count of the number of array elements
     * @param array     Array instance or <code>null</code> to retrieve size of the array
     * @return Vulkan result code
     */
    int enumerate(VulkanLibrary lib, IntByReference count, T array);
}
```

We can now define the API call as follows:

```java
/**
 * @return Supported extensions function
 */
public VulkanFunction<VkExtensionProperties> extensions() {
    return (api, count, extensions) -> api.vkEnumerateDeviceExtensionProperties(handle, null, count, extensions);
}
```

We add a helper that implements two-stage invocation for an array of structures:

```java
static <T extends Structure> T[] enumerate(VulkanFunction<T> func, VulkanLibrary lib, Supplier<T> identity) {
    // Count number of values
    final IntByReference count = lib.factory().integer();
    check(func.enumerate(lib, count, null));

    // Retrieve values
    if(count.getValue() > 0) {
        final T[] array = (T[]) identity.get().toArray(count.getValue());
        check(func.enumerate(lib, count, array[0]));
        return array;
    }
    else {
        return (T[]) Array.newInstance(identity.getClass(), 0);
    }
}
```

The _identity_ supplies an instance of the structure used to allocate the resultant array using the JNA `toArray()` approach.

This can then be used as follows:

```java
final VulkanFunction<VkExtensionProperties> func = ...
final VkExtensionProperties[] extensions = VulkanFunction.enumerate(func, lib, VkExtensionProperties::new);
```

The interface also provides a more general implementation for an arbitrarily typed array:

```java
static <T> T[] enumerate(VulkanFunction<T[]> func, VulkanLibrary lib, IntFunction<T[]> factory) {
    // Determine array length
    final IntByReference count = lib.factory().integer();
    check(func.enumerate(lib, count, null));

    // Allocate array
    final T[] array = factory.apply(count.getValue());

    // Retrieve array
    if(array.length > 0) {
        check(func.enumerate(lib, count, array));
    }

    return array;
}
```

---

## Vulkan Structure Arrays

Vulkan makes heavy use of structures to configure a variety of objects (e.g. the pipeline) and we are also often required to allocate and populate arrays of these structures.

Unlike a standard POJO an array of JNA structures **must** be allocated using the `toArray()` helper method which introduces complications:

- We often have to handle the case where the array is empty (or even `null`).

- We cannot simply transform a collection of domain objects to an array using (for example) the stream `toArray()` method (since the JNA array must be a contiguous block of memory).

- Many native methods expect a pointer-to-array value, i.e. the **first** element of the array.

Whilst none of the above is particularly difficult to overcome it can be tedious, error-prone, and less testable.

Therefore we provide another helper on the base-class structure that separates these aspects:

```java
/**
 * Helper - Allocates and populates an array of the given Vulkan structure as a contiguous memory block.
 * @param <R> Structure type
 * @param <T> Source data type
 * @param ctor            Constructor
 * @param data            Data
 * @param populate        Population function
 * @return <b>First</b> element of the new array
 */
public static <R extends VulkanStructure, T> R populate(Supplier<R> ctor, Collection<T> data, BiConsumer<T, R> populate) {
    // Check for empty data
    if(data.isEmpty()) {
        return null;
    }

    // Allocate array
    final R[] array = array(ctor, data.size());

    // Populate array
    final Iterator<T> itr = data.iterator();
    for(int n = 0; n < array.length; ++n) {
        populate.accept(itr.next(), array[n]);
    }

    return array[0];
}
```

Example usage:

```java
class ClearValue {
    void populate(VkClearValue value) {
        ...
    }
}

info.clearValueCount = values.size();
info.pClearValues = VulkanStructure.populate(VkClearValue::new, values, ClearValue::populate);
```

This approach separates the array allocation, the iteration logic, and the process of populating an array element, reducing the potential for cock-ups and improving testability.

Note that `populate()` returns the **first** element as this is the common approach for a JNA structure array.
