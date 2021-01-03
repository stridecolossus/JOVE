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

---

