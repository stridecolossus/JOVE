---
title: Memory Allocator
---

## Overview

In the previous chapter we skipped over the process of allocating device memory for the vertex buffers.
Here we will flesh out the memory allocator which will also be used in the next chapter for allocation of texture memory.

However the maximum number of memory allocations supported by the hardware is limited.
A real-world application would allocate a memory _pool_ and then serve allocation requests as offsets into that pool (growing the available memory as required).

Initially we will simply allocate a new memory block for every request and once that is working extend our implementation to use a memory pool.

---

## Memory Allocator

### Memory Request

We start with a basic abstraction for device memory based on our understanding of the requirements for vertex buffers in the previous chapter:

```java
public interface DeviceMemory extends TransientNativeObject {
    /**
     * @return Memory offset
     */
    long offset();

    /**
     * @return Size of this memory
     */
    long size();
}
```

And a template implementation:

```java
abstract class AbstractDeviceMemory extends AbstractTransientNativeObject implements DeviceMemory {
    private final long size;
    private final long offset;

    protected AbstractMemory(Pointer handle, long size, long offset) {
        super(handle);
        this.size = oneOrMore(size);
        this.offset = zeroOrMore(offset);
    }

    @Override
    public long offset() {
        return offset;
    }

    @Override
    public long size() {
        return size;
    }
}
```

Next we implement an outline for the memory allocator service:

```java
public class MemoryAllocator {
    public Request request() {
        return new Request();
    }

    public class Request {
        private Request() {
        }

        public DeviceMemory allocate() {
        }
    }
}
```

The nested request class is used by the application to specify the memory requirements using a builder-like interface:

```java
public class Request {
    private long size;
    private int filter = Integer.MAX_VALUE;
    private final Set<VkMemoryPropertyFlag> flags = new HashSet<>();

    private Request() {
    }

    /**
     * Sets the required size of the memory.
     * @param size Required memory size (bytes)
     */
    public Request size(long size) {
        this.size = oneOrMore(size);
        return this;
    }

    /**
     * Sets the memory type filter bit-mask.
     * @param filter Memory type filter mask
     */
    public Request filter(int filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Convenience method to initialise this allocation to the given memory requirements descriptor.
     * @param reqs Memory requirements
     */
    public Request init(VkMemoryRequirements reqs) {
        size(reqs.size);
        filter(reqs.memoryTypeBits);
        return this;
    }

    /**
     * Adds a memory property.
     * @param flag Memory property
     */
    public Request property(VkMemoryPropertyFlag flag) {
        flags.add(notNull(flag));
        return this;
    }
}
```

The _filter_ field specifies the _memory types_ required for the allocation request as a bit-field - we will cover the purpose of this shortly.

We also provide the convenience `init` method to initialise the request to the `VkMemoryRequirements` retrieved from Vulkan when the VBO (or texture) is instantiated.

### Memory Allocation

With the outline code in place we can now integrate the API methods for memory allocation.

The first step is to retrieve the `VkPhysicalDeviceMemoryProperties` that specifies the available _memory types_ supported by the hardware:

```java
public class MemoryAllocator {
    private final LogicalDevice dev;
    private final VkPhysicalDeviceMemoryProperties props = new VkPhysicalDeviceMemoryProperties();

    MemoryAllocator(LogicalDevice dev) {
        this.dev = notNull(dev);
        dev.library().vkGetPhysicalDeviceMemoryProperties(dev.parent().handle(), props);
    }
}
```

The [VkPhysicalDeviceMemoryProperties](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html)
documentation outlines the process of selecting the appropriate memory type for a given allocation request.

In summary this process is as follows:
1. Walk through the array of memory types in the `VkPhysicalDeviceMemoryProperties` structure.
2. Filter the elements by _index_ using the _filter_ bit-mask from the `VkMemoryRequirements` of the object we are allocating memory for.
3. Then filter by matching the supported properties of each memory type against those requested.

We wrap this logic into a helper method on the request class (and introduce a custom exception class):

```java
private int find() throws AllocationException {
    final int mask = IntegerEnumeration.mask(flags);
    for(int n = 0; n < props.memoryTypes.length; ++n) {
        if(MathsUtil.isBit(filter, n) && MathsUtil.isMask(props.memoryTypes[n].propertyFlags, mask)) {
            return n;
        }
    }
    throw new AllocationException("No memory type available:" + this);
}
```

Finally we can allocate the memory:

```java
public DeviceMemory allocate() {
    // Determine memory type
    if(size == 0) throw new IllegalArgumentException("Memory size not specified");
    final int type = find();

    // Init memory descriptor
    final VkMemoryAllocateInfo info = new VkMemoryAllocateInfo();
    info.allocationSize = size;
    info.memoryTypeIndex = type;

    // Allocate memory
    final VulkanLibrary lib = dev.library();
    final PointerByReference ptr = lib.factory().pointer();
    check(lib.vkAllocateMemory(dev.handle(), info, null, ptr));
    
    ...
}
```

And create the domain object for the allocated memory:

```java
return new AbstractDeviceMemory(ptr.getValue(), size, 0) {
    @Override
    protected void release() {
        dev.library().vkFreeMemory(dev.handle(), handle, null);
    }
};
```








In addition the documentation suggests implementing a fallback strategy, i.e. the application requests _optimal_ and _fallback_ properties for the memory.











        public Memory allocate() throws AllocationException {
            if(size == 0) throw new IllegalArgumentException("Memory size not specified");
            final Type type = find();
            return type.pool.allocate(size); // TODO - page
        }


            // Init memory descriptor
            final VkMemoryAllocateInfo info = new VkMemoryAllocateInfo();
            info.allocationSize = size;
            info.memoryTypeIndex = index;

            // Allocate memory
            final VulkanLibrary lib = dev.library();
            final PointerByReference mem = lib.factory().pointer();
            check(lib.vkAllocateMemory(dev.handle(), info, null, mem));



---

## Memory Pool

---

## Summary

In this chapter we designed and implemented the device memory allocator using a memory pool.
