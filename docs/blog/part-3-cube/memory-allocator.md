---
title: Memory Allocation
---

## Overview

In the following chapters we will be implementing vertex buffers and textures, both of which are dependant on _device memory_ allocated by Vulkan.

The [Vulkan documentation](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html) suggests implementing a _fallback strategy_ when selecting the memory type.  The application requests _optimal_ and _minimal_ properties for the required memory, with the algorithm falling back to the minimal properties if the optimal memory type is not available.

Additionally the maximum number of memory allocations supported by the hardware is limited.  A real-world application would allocate a memory _pool_ and then serve allocation requests as offsets into that pool (growing the available memory as required).  Initially we will allocate a new memory block for every request and then extend the implementation to use a memory pool.

References:

- [VkPhysicalDeviceMemoryProperties](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html)
- [Custom Allocators (Blog)](http://kylehalladay.com/blog/tutorial/2017/12/13/Custom-Allocators-Vulkan.html)

---

## Device Memory

### Definition

We start with an abstraction for device memory which will have two implementations:

1. A public class used in vertex buffers and texture images.

2. An internal implementation for a _memory block_ allocated from the pool.

Device memory is defined as follows:

```java
public interface DeviceMemory extends TransientNativeObject {
    /**
     * A <i>region</i> is a mapped area of device memory.
     */
    public interface Region {
        ...
    }

    /**
     * @return Size of this memory (bytes)
     */
    long size();

    /**
     * @return Mapped memory region
     */
    Optional<Region> region();

    /**
     * Maps a region of this device memory.
     * @param offset        Offset into this memory
     * @param size          Size of the region to map
     * @return Mapped memory region
     * @throws IllegalArgumentException if the {@code offset} and {@code size} exceeds the size of this memory
     * @throws IllegalStateException if a mapping already exists or this memory has been destroyed
     */
    Region map(long offset, long size);
}
```

A _region_ is a _mapped_ segment of this memory that can be accessed for read or write operations:

```java
public interface Region {
    /**
     * @return Size of this region (bytes)
     */
    long size();

    /**
     * Provides a byte-buffer to access a sub-section of this memory region.
     * @param offset        Offset
     * @param size          Region size (bytes)
     * @return Byte-buffer
     * @throws IllegalArgumentException if the {@code offset} and {@code size} exceeds the size of this region
     */
    ByteBuffer buffer(long offset, long size);

    /**
     * Un-maps this mapped region.
     * @throws IllegalStateException if the mapping has also been released or the memory has been destroyed
     */
    void unmap();
}
```

### Default Implementation

We create a new domain object for the public implementation:

```java
public class DefaultDeviceMemory extends AbstractVulkanObject implements DeviceMemory {
    private final long len;

    public DefaultDeviceMemory(Pointer handle, DeviceContext dev, long len) {
        super(handle, dev);
        this.len = oneOrMore(len);
    }

    @Override
    public long size() {
        return len;
    }

    @Override
    protected Destructor<DefaultDeviceMemory> destructor(VulkanLibrary lib) {
        return lib::vkFreeMemory;
    }
}
```

And a local class for the implementation of the mapped region:

```java
public class DefaultDeviceMemory ... {
    private volatile DefaultRegion region;

    private class DefaultRegion implements Region {
        private final Pointer ptr;
        private final long size;
        private final long offset;
    }

    @Override
    public Optional<Region> region() {
        return Optional.ofNullable(region);
    }

    @Override
    protected void release() {
        region = null;
    }
}
```

The `map` method creates a mapped region for the memory:

```java
public synchronized Region map(long offset, long size) {
    // Validate
    ...

    // Map memory
    final DeviceContext dev = this.device();
    final VulkanLibrary lib = dev.library();
    final PointerByReference ref = lib.factory().pointer();
    check(lib.vkMapMemory(dev, this, offset, size, 0, ref));

    // Create mapped region
    region = new DefaultRegion(ref.getValue(), offset, size);

    return region;
}
```

Note that only one mapped region is permitted for a given device memory object.

An NIO buffer can then be retrieved from a mapped region to read or write to the memory:

```java
public ByteBuffer buffer(long offset, long size) {
    checkMapped();
    if(offset + size > this.size) throw new IllegalArgumentException(...);
    return ptr.getByteBuffer(offset, size);
}
```

Finally a mapped region can be released:

```java
public synchronized void unmap() {
    // Validate mapping is active
    checkMapped();

    // Release mapping
    final DeviceContext dev = device();
    final VulkanLibrary lib = dev.library();
    lib.vkUnmapMemory(dev, DefaultDeviceMemory.this);

    // Clear mapping
    region = null;
}
```

### Memory Types

The memory types supported by the hardware are specified by the `VkPhysicalDeviceMemoryProperties` descriptor.

We represent each memory type by a new domain record:

```java
public record MemoryType(int index, Heap heap, Set<VkMemoryProperty> properties) {
}
```

With a local class for the memory heap:

```java
public static class Heap {
    private final int index;
    private final long size;
    private final Set<VkMemoryHeapFlag> flags;
}
```

The memory types are generated from the two arrays in the descriptor by the following factory:

```java
public static List<MemoryType> enumerate(VkPhysicalDeviceMemoryProperties props) {
}
```

First the memory heaps is transformed to the domain object:

```java
    // Enumerate memory heaps
    final IntFunction<Heap> heapMapper = index -> {
        final VkMemoryHeap heap = props.memoryHeaps[index];
        final var flags = IntegerEnumeration.enumerate(VkMemoryHeapFlag.class, heap.flags);
        return new Heap(index, heap.size, flags);
    };
    final Heap[] heaps = new Heap[props.memoryHeapCount];
    Arrays.setAll(heaps, heapMapper);

    ...
}
```

Similarly for the memory types:

```java
// Enumerate memory types
final IntFunction<MemoryType> typeMapper = index -> {
    final VkMemoryType type = props.memoryTypes[index];
    final Heap heap = heaps[type.heapIndex];
    final var properties = IntegerEnumeration.enumerate(VkMemoryProperty.class, type.propertyFlags);
    return new MemoryType(index, heap, properties);
};
final MemoryType[] types = new MemoryType[props.memoryTypeCount];
Arrays.setAll(types, typeMapper);

// Convert to collection
return Arrays.asList(types);
```

Note that the `index` of each memory type is implicitly the array index.

The final supporting component needed before memory allocation can be implemented is a _memory properties_ record:

```java
public record MemoryProperties<T>(Set<T> usage, VkSharingMode mode, Set<VkMemoryProperty> required, Set<VkMemoryProperty> optimal) {
    public static class Builder {
        ...
    }
}
```

This new type specifies the intent of the memory and the _optimal_ and _required_ properties for the request.  Note that the record is generic based on the relevant usage enumeration, e.g. `VkImageUsage` for device memory used by an image.

### Memory Allocation

Next we define the _allocator_ that is responsible for allocating memory of a given type:

```java
@FunctionalInterface
public interface Allocator {
    /**
     * An <i>allocation exception</i> is thrown when this allocator cannot allocate memory.
     */
    class AllocationException extends RuntimeException {
        ...
    }

    /**
     * Allocates device memory.
     * @param type Type of memory to allocate
     * @param size Size of the memory (bytes)
     * @return New device memory
     * @throws AllocationException if the memory cannot be allocated
     */
    DeviceMemory allocate(MemoryType type, long size) throws AllocationException;
}
```

We provide a factory to create a default implementation that simply allocates a new memory block on each request:

```java
static Allocator allocator(DeviceContext dev) {
    return (type, size) -> {
        // Init memory descriptor
        final var info = new VkMemoryAllocateInfo();
        info.allocationSize = oneOrMore(size);
        info.memoryTypeIndex = type.index();

        // Allocate memory
        final VulkanLibrary lib = dev.library();
        final PointerByReference ref = lib.factory().pointer();
        check(lib.vkAllocateMemory(dev, info, null, ref));

        // Create memory wrapper
        return new DefaultDeviceMemory(ref.getValue(), dev, size);
    };
}
```

Later in the chapter we will provide further implementations to support a memory pool and pagination.

Note that the actual size of the allocated memory may be larger than the requested length depending on how the hardware handles alignment.

All the above components are brought together into the following service which is the entry-point for allocation of device memory:

```java
public class AllocationService {
    private final Allocator allocator;
    private final List<MemoryType> types;

    public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
        ...
    }
}
```

Creating this service is comprised of the following steps:

1. Retrieve the descriptor for the memory types supported by the hardware.

2. Transform this to the memory type and heap domain objects.

3. Instantiate the service.

As normal this is implemented as a factory method:

```java
public static AllocationService create(LogicalDevice dev, Allocator allocator) {
    // Retrieve supported memory types
    final var props = new VkPhysicalDeviceMemoryProperties();
    final VulkanLibrary lib = dev.library();
    lib.vkGetPhysicalDeviceMemoryProperties(dev.parent(), props);

    // Enumerate memory types
    final List<MemoryType> types = MemoryType.enumerate(props);

    // Create service
    return new AllocationService(allocator, types);
}
```

The Vulkan documentation outlines the suggested approach for selecting the memory type for a given allocation request as follows:

1. Walk through the supported memory types.

2. Filter by _index_ using the _filter_ bit-mask from the `VkMemoryRequirements` of the object in question.

3. Filter by the supported memory properties of each type (i.e. matching against either the _optimal_ properties or falling back to the minimal _required_ properties).

The `allocate` method first enumerates the _candidate_ memory types by applying the filter:

```java
public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
    final var candidates = types
        .stream()
        .filter(type -> MathsUtil.isBit(reqs.memoryTypeBits, type.index()))
        .collect(toList());

    ...
}
```

Next the best available type is selected from these candidates:

```java
final MemoryType type =
    find(candidates, props.optimal())
    .or(() -> find(candidates, props.required()))
    .orElseThrow(() -> new AllocationException(...));
```

And finally we can allocate the memory of the selected type:

```java
return allocator.allocate(type, reqs.size);
```

The `find` method is a helper that applies the memory properties matching logic:

```java
private static Optional<MemoryType> find(List<MemoryType> types, Set<VkMemoryProperty> props) {
    if(props.isEmpty()) {
        return Optional.empty();
    }

    return types
        .stream()
        .filter(type -> type.properties().containsAll(props))
        .findAny();
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

### Pagination

### Integration


```java
@Bean
public static Allocator allocator(LogicalDevice dev, @Value("${memory.page.size}") long page) {
    final Allocator allocator = Allocator.allocator(dev);
    return new PageAllocator(allocator, page);
}
```

```java
@Bean
public static AllocationService allocationService(LogicalDevice dev, Allocator allocator) {
    return AllocationService.create(dev, allocator);
}
```




---

## Summary

In this chapter we:

- Implemented the framework for memory allocation.

TODO

