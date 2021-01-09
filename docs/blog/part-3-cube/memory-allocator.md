---
title: Memory Allocator
---

## Overview

In the previous chapter we skipped over the process of allocating device memory for the vertex buffers.
Here we will flesh out the memory allocator which will also be used in the next chapter for allocation of texture memory.

However the maximum number of memory allocations supported by the hardware is limited.
A real-world application would allocate a memory _pool_ and then serve allocation requests as offsets into that pool (growing the available memory as required).

Initially we will simply allocate a new memory block for every request and once that is working extend our implementation to use a memory pool.

References:

- [VkPhysicalDeviceMemoryProperties](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html)
- [Custom Allocators (Blog)](http://kylehalladay.com/blog/tutorial/2017/12/13/Custom-Allocators-Vulkan.html)

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

    public Request size(long size) {
        this.size = oneOrMore(size);
        return this;
    }

    public Request filter(int filter) {
        this.filter = filter;
        return this;
    }

    public Request init(VkMemoryRequirements reqs) {
        size(reqs.size);
        filter(reqs.memoryTypeBits);
        return this;
    }

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

The documentation outlines the suggested approach for selecting the memory type for a given memory allocation:

1. Walk through the array of memory types in the `VkPhysicalDeviceMemoryProperties` structure.
2. Filter the elements by _index_ using the _filter_ bit-mask from the `VkMemoryRequirements` of the object we are allocating memory for.
3. Filter by matching the supported properties of each memory type against those requested.

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
    ...
    return new AbstractDeviceMemory(ptr.getValue(), size, 0) {
        @Override
        protected void release() {
            dev.library().vkFreeMemory(dev.handle(), handle, null);
        }
    };
}
```

### Fallback Strategy

The documentation also suggests implementing a _fallback strategy_ when selecting the memory type.
The application requests _optimal_ and _minimal_ properties for the memory with the algorithm falling back to the minimal (or required) properties if the optimal settings are not available.

We first refactor the `find` method to accept an arbitrary set of properties and return an optional result:

```java
private Optional<Integer> find(Set<VkMemoryPropertyFlag> flags) {
    final int mask = IntegerEnumeration.mask(flags);
    for(int n = 0; n < props.memoryTypes.length; ++n) {
        if(MathsUtil.isBit(filter, n) && MathsUtil.isMask(props.memoryTypes[n].propertyFlags, mask)) {
            return Optional.of(n);
        }
    }
    return Optional.empty();
}
```

This can then be used in `allocate` to match on the optimal memory properties or fallback to the minimal requirements:

```java
public class Request {
    private final Set<VkMemoryPropertyFlag> optimal = new HashSet<>();
    private final Set<VkMemoryPropertyFlag> required = new HashSet<>();
    ...

    public DeviceMemory allocate() {
        final int index = find(optimal)
                .or(() -> find(required))
                .orElseThrow(() -> new AllocationException(...);
            
        ...
    }
}
```

---

## Memory Pool

### Overview

Our requirements for the memory pool are as follows:

- One pool per memory type.

- The memory managed by the pool automatically grows as required.

- Pools can also be initialised, i.e. pre-allocate the expected amount of memory required the application (to reduce the number of allocations and avoid fragmentation).

- Memory allocated by the pool can be destroyed by the application and returned to the pool (and potentially re-used).

- The allocator and pool(s) should provide useful statistics to the application, e.g. number of allocations, free memory, etc.

Additionally the `VkPhysicalDeviceLimits` structure contains a `bufferImageGranularity` property which we will use to size allocation 'pages' when the pool grows.

### Implementation

The first design decision is that the pool will be Vulkan-agnostic amd separate from the allocator itself.
It is unlikely that we will re-use the memory pool elsewhere but separating the two concerns will reduce the overall complexity and simplify testing.

The first-cut class outline for the pool is as follows:

```java
class Pool {
    public static class AllocationException extends RuntimeException {
        ...
    }

    /**
     * A <i>memory allocator</i> allocates new memory blocks on demand.
     */
    @FunctionalInterface
    public interface Allocator {
        /**
         * Allocates a new block of memory.
         * @param size Memory size
         * @return Pointer to the new memory
         * @throws AllocationException if the memory cannot be allocated
         */
        DeviceMemory allocate(long size) throws AllocationException;
    }
    
    /**
     * Adds free memory to this pool.
     * @param size Memory to add
     * @throws IllegalArgumentException if the given size is zero-or-less
     * @throws AllocationException if the memory cannot be allocated
     */
    public void add(long size) throws AllocationException {
    }

    /**
     * Allocates memory from this pool.
     * @param size Amount of memory to allocate
     * @return New memory
     * @throws IllegalArgumentException if the given size is zero-or-less
     * @throws AllocationException if the memory cannot be allocated by this pool
     */
    public DeviceMemory allocate(long size) throws AllocationException {
    }
}
```

The `Allocator` is responsible for actually allocating a new block of memory as required by the pool.

### Integration

Before we implement the main functionality of the pool we will integrate this first-cut into the Vulkan allocator and create an instance per memory type.

We will also implement local domain classes for the memory heaps and types instead of using the `VkPhysicalDeviceMemoryProperties` structure directly.
Whilst this is not really necessary it makes for slightly neater code and means we have logical places for pool initialisation and statistics (rather than messing about with array indices).

The new domain objects basically replicate the data from the structure:

```java
public class MemoryAllocator {
    /**
     * A <i>memory heap</i> specifies the properties of a group of device memory types.
     */
    public static final class Heap {
        private final int index;
        private final long size;
        private final Set<VkMemoryPropertyFlag> props;
        private final List<Type> types = new ArrayList<>();
    }

    /**
     * A <i>memory type</i> defines a type of device memory supported by the hardware.
     */
    public final class Type {
        private final int index;
        private final Heap heap;
        private final int props;
    }
}
```

Next we refactor the constructor to a static factory method:

```java
public static MemoryAllocator create(LogicalDevice dev) {
        // Retrieve memory properties for this device
        final var props = new VkPhysicalDeviceMemoryProperties();
        dev.library().vkGetPhysicalDeviceMemoryProperties(dev.parent().handle(), props);
        
        ...

}
```

```java
        final List<Heap> heaps = new ArrayList<>();
        for(int n = 0; n < props.memoryHeapCount; ++n) {
            final VkMemoryHeap h = props.memoryHeaps[n];
            final var flags = IntegerEnumeration.enumerate(VkMemoryPropertyFlag.class, h.flags);
            final Heap heap = new Heap(n, h.size, flags);
            heaps.add(heap);
        }

        // Enumerate memory types array
        final Type[] array = new Type[props.memoryTypeCount];
        for(int n = 0; n < array.length; ++n) {
            final VkMemoryType info = props.memoryTypes[n];
            final Heap heap = heaps.get(info.heapIndex);
            array[n] = new Type(n, heap, info.propertyFlags);
        }
        this.types = Arrays.asList(array);

        // Retrieve global memory parameters
        final VkPhysicalDeviceLimits limits = dev.parent().properties().limits();
        this.max = limits.maxMemoryAllocationCount;
        this.page = limits.bufferImageGranularity;
        this.dev = dev;
    }
```




However note that we intentionally made the objects inter-dependant

refactor using heap/type

allocates blocks
allocations offset into block

paging
stream
stats

### Memory Blocks



---

## Summary

In this chapter we designed and implemented the device memory allocator using a memory pool.

The API methods to manage device memory are defined in the `VulkanLibraryMemory` JNA interface.
