---
title: Memory Allocation
---

---

## Contents

- [Overview](#overview)
- [Device Memory](#device-memory)
- [Memory Pool](#memory-pool)
- [Allocation Strategy](#allocation-strategy)

---

## Overview

The following chapters will introduce vertex buffers and textures, both of which are dependant on _device memory_ allocated by Vulkan.  Device memory resides on the hardware and can optionally be visible to the host (i.e. the application) depending on the usage requirements.

A Vulkan implementation specifies a set of _memory types_ from which an application can select depending on the usage scenario.  The documentation suggests implementing a _fallback strategy_ when choosing a memory type: the application specifies _optimal_ and _minimal_ properties for the requested memory, with the algorithm falling back to the minimal properties if the optimal memory type is not available.

Additionally the maximum number of memory allocations supported by the hardware is limited.  A real-world application would allocate a memory _pool_ and then serve allocation requests as offsets into that pool, growing the available memory as required.  Initially we will allocate a new memory instance for each request and then extend the implementation to use a memory pool.

References:
- [VkPhysicalDeviceMemoryProperties](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html)
- [Custom Allocators (Blog)](http://kylehalladay.com/blog/tutorial/2017/12/13/Custom-Allocators-Vulkan.html)

---

## Device Memory

### Definition

We start with the following abstraction for device memory:

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
interface Region {
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

Next a default implementation is created:

```java
public class DefaultDeviceMemory extends AbstractVulkanObject implements DeviceMemory {
    private final long size;

    @Override
    protected Destructor<DefaultDeviceMemory> destructor(VulkanLibrary lib) {
        return lib::vkFreeMemory;
    }
}
```

With an inner class for the mapped region of the memory:

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

The `map` method creates the mapped region for the memory:

```java
public Region map(long offset, long size) {
    // Map memory
    DeviceContext dev = this.device();
    VulkanLibrary lib = dev.library();
    PointerByReference ref = dev.factory().pointer();
    check(lib.vkMapMemory(dev, this, offset, size, 0, ref));

    // Create mapped region
    region = new DefaultRegion(ref.getValue(), offset, size);

    return region;
}
```

Note that only one mapped region is permitted for a given device memory instance.

An NIO buffer can then be retrieved from the mapped region to read or write to the memory:

```java
public ByteBuffer buffer(long offset, long size) {
    return ptr.getByteBuffer(offset, size);
}
```

Finally the mapped region can be released:

```java
public void unmap() {
    DeviceContext dev = device();
    VulkanLibrary lib = dev.library();
    lib.vkUnmapMemory(dev, DefaultDeviceMemory.this);
    region = null;
}
```

### Memory Types

The memory types supported by the hardware are specified by the `VkPhysicalDeviceMemoryProperties` descriptor.

Each memory type is represented by a new domain class:

```java
public record MemoryType(int index, Heap heap, Set<VkMemoryProperty> properties) {
    public record Heap(long size, Set<VkMemoryHeapFlag> flags) {
    }
}
```

The memory types and heaps are enumerated from the two arrays in the descriptor by a factory method:

```java
public static MemoryType[] enumerate(VkPhysicalDeviceMemoryProperties props) {
    // Extract heaps
    Heap[] heaps = new Heap[props.memoryHeapCount];
    var heapMapper = IntegerEnumeration.mapping(VkMemoryHeapFlag.class);
    for(int n = 0; n < heaps.length; ++n) {
        VkMemoryHeap heap = props.memoryHeaps[n];
        Set<VkMemoryHeapFlag> flags = heapMapper.enumerate(heap.flags);
        heaps[n] = new Heap(heap.size, flags);
    }

    // Extract memory types
    MemoryType[] types = new MemoryType[props.memoryTypeCount];
    var typeMapper = IntegerEnumeration.mapping(VkMemoryProperty.class);
    for(int n = 0; n < types.length; ++n) {
        VkMemoryType type = props.memoryTypes[n];
        Heap heap = heaps[type.heapIndex];
        Set<VkMemoryProperty> properties = typeMapper.enumerate(type.propertyFlags);
        types[n] = new MemoryType(n, heap, properties);
    }

    return types;
}
```

Finally the following new type specifies the requirements of a memory request:

```java
public record MemoryProperties<T>(
    Set<T> usage,
    VkSharingMode mode,
    Set<VkMemoryProperty> required,
    Set<VkMemoryProperty> optimal
)
```

Note that this type is generic based on the relevant usage enumeration, e.g. `VkImageUsage` for device memory used by an image.

The constructor enforces the _optimal_ properties to be a super-set of the _required_ properties.

```java
public MemoryProperties {
    ...
    optimal = Set.copyOf(CollectionUtils.union(required, optimal));
}
```

### Memory Selection

The Vulkan documentation outlines the suggested approach for selecting the appropriate memory type for a given allocation request as follows:

1. Walk through the supported memory types.

2. Filter by _index_ using the _filter_ bit-mask from the `VkMemoryRequirements` of the object in question.

3. Filter by matching the supported memory properties against the _optimal_ properties of the request.

4. Or fall back to the _required_ properties.

This algorithm is encapsulated in a new component:

```java
public class MemorySelector {
    private final MemoryType[] types;

    /**
     * Selects the memory type for the given request.
     * @param reqs          Requirements
     * @param props         Memory properties
     * @return Selected memory type
     * @throws AllocationException if no memory type matches the request
     */
    public MemoryType select(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
        ...
    }
}
```

The `select` method first enumerates the _candidate_ memory types by applying the filter:

```java
List<MemoryType> candidates = IntStream
    .range(0, types.length)
    .filter(n -> ((1 << n) & reqs.memoryTypeBits) == n)
    .mapToObj(n -> types[n])
    .toList();
```

Next the best available type is selected from these candidates:

```java
MemoryType type =
    find(candidates, props.optimal())
    .or(() -> find(candidates, props.required()))
    .orElseThrow(() -> new AllocationException(...));
```

Where the `find` method is a helper which matches the memory properties:

```java
private static Optional<MemoryType> find(List<MemoryType> types, Set<VkMemoryProperty> props) {
    return types
        .stream()
        .filter(type -> type.properties().containsAll(props))
        .findAny();
}
```

A memory selector is created and configured via a factory method:

```java
public static MemorySelector create(LogicalDevice dev) {
    // Retrieve supported memory types
    var props = new VkPhysicalDeviceMemoryProperties();
    VulkanLibrary lib = dev.library();
    lib.vkGetPhysicalDeviceMemoryProperties(dev.parent(), props);

    // Enumerate memory types
    MemoryType[] types = MemoryType.enumerate(props);

    // Create selector
    return new MemorySelector(types);
}
```

### Memory Allocation

The second abstraction is an _allocator_ which is responsible for allocating memory of a given type:

```java
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

The default implementation simply allocates new memory on demand:

```java
public class DefaultAllocator implements Allocator {
    private final DeviceContext dev;

    @Override
    public DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
        // Init memory descriptor
        var info = new VkMemoryAllocateInfo();
        info.allocationSize = oneOrMore(size);
        info.memoryTypeIndex = type.index();

        // Allocate memory
        VulkanLibrary lib = dev.library();
        PointerByReference ref = dev.factory().pointer();
        check(lib.vkAllocateMemory(dev, info, null, ref));

        // Create memory wrapper
        return new DefaultDeviceMemory(ref.getValue(), dev, size);
    }
}
```

Note that the actual size of the allocated memory may be larger than the requested length depending on how the hardware handles alignment.

The selector and allocator are composed into a new service that is responsible for memory allocation:

```java
public class AllocationService {
    private final MemorySelector selector;
    private final Allocator allocator;

    public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
        MemoryType type = selector.select(reqs, props);
        return allocator.allocate(type, reqs.size);
    }
}
```

Finally a new API is added for memory management:

```java
interface Library {
    int  vkAllocateMemory(DeviceContext device, VkMemoryAllocateInfo pAllocateInfo, Pointer pAllocator, PointerByReference pMemory);
    void vkFreeMemory(DeviceContext device, DefaultDeviceMemory memory, Pointer pAllocator);
    int  vkMapMemory(DeviceContext device, DefaultDeviceMemory memory, long offset, long size, int flags, PointerByReference ppData);
    void vkUnmapMemory(DeviceContext device, DefaultDeviceMemory memory);
}
```

---

## Memory Pool

### Overview

With a basic memory framework in place we can now implement the memory pool.

The requirements for the pool are as follows:

- One pool per memory type.

- A pool grows as required.

- Memory can be pre-allocated to reduce the number of allocations and avoid fragmentation.

- Memory allocated by the pool can be destroyed by the application and potentially re-allocated.

- The allocator and pool(s) should provide useful statistics to the application, e.g. number of allocations, free memory, etc.

To support these requirements a second memory implementation is introduced for a _block_ of memory managed by the pool, requests are then served from a block with available free memory or new blocks are created on demand.

### Memory Blocks

A _block_ is a wrapper for a device memory instance and manages allocations from the block:

```java
class Block {
    private final DeviceMemory mem;
    private final List<BlockDeviceMemory> allocations = new ArrayList<>();
    private long next;

    class BlockDeviceMemory implements DeviceMemory {
        ...
    }

    void destroy() {
        mem.destroy();
        allocations.clear();
    }
}
```

Memory requests are allocated from the end of the block indicated by the `next` free-space offset:

```java
BlockDeviceMemory allocate(long size) {
    // Allocate from free space
    BlockDeviceMemory alloc = new BlockDeviceMemory(next, size);
    allocations.add(alloc);

    // Update free space pointer
    next += size;
    assert next <= mem.size();

    return alloc;
}
```

The memory allocated from the block is an inner class:

```java
class BlockDeviceMemory implements DeviceMemory {
    private final long offset;
    private final long size;
    private boolean destroyed;

    @Override
    public long size() {
        return size;
    }
    
    @Override
    public void destroy() {
        destroyed = true;
    }
}
```

This implementation is essentially a proxy to the device memory of the parent block:

```java
class BlockDeviceMemory implements DeviceMemory {
    ...
    
    @Override
    public Handle handle() {
        return mem.handle();
    }

    @Override
    public Optional<Region> region() {
        return mem.region();
    }

    @Override
    public Region map(long offset, long size) {
        mem.region().ifPresent(Region::unmap);
        return mem.map(offset, size);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed || mem.isDestroyed();
    }
}
```

Note that since only one region mapping is allowed per memory instance the `map` method silently releases the previous mapping (if any).

Destroyed allocations are not removed from the block but are only marked as released and can be reallocated:

```java
BlockDeviceMemory reallocate() {
    if(!destroyed) throw new IllegalStateException(...);
    if(mem.isDestroyed()) throw new IllegalStateException(...);
    destroyed = false;
    return this;
}
```

Finally the amount of available memory in the block is queried using the following method:

```java
long free() {
    long total = allocations
        .stream()
        .filter(ALIVE)
        .mapToLong(DeviceMemory::size)
        .sum();
        
    return mem.size() - total;
}
```

Where `ALIVE` is a simple helper constant:

```java
Predicate<DeviceMemory> ALIVE = Predicate.not(DeviceMemory::isDestroyed);
```

### Pool

Each memory pool is comprised of a number of blocks from which memory requests are served:

```java
public class MemoryPool {
    private final MemoryType type;
    private final Allocator allocator;
    private final List<Block> blocks = new ArrayList<>();
    private long total;

    DeviceMemory allocate(long size) {
        ...
    }
}
```

The free space in the pool is queried as follows:

```java
public long free() {
    return blocks
        .stream()
        .mapToLong(Block::free)
        .sum();
}
```

All memory allocations can be released back to the pool:

```java
public void release() {
    Stream<? extends DeviceMemory> allocations = this.allocations();
    allocations.forEach(DeviceMemory::close);
    assert free() == total;
}
```

Which uses the following helper:

```java
Stream<? extends DeviceMemory> allocations() {
    return blocks
        .stream()
        .flatMap(Block::allocations)
        .filter(Block.ALIVE);
}
```

Destroying the pool also destroys the allocated memory blocks:

```java
public void destroy() {
    for(Block b : blocks) {
        b.destroy();
    }
    blocks.clear();
    total = 0;
    assert free() == 0;
}
```

To service an allocation request the pool tries the following in order:

1. Find a block with sufficient free memory.

2. Or find a released allocation that can be re-allocated.

3. Otherwise allocate a new block.

This is implemented as follows:

```java
private DeviceMemory allocate(long size) {
    // Short cut to allocate a new block if pool has insufficient free memory
    if(free() < size) {
        return allocateNewBlock(size);
    }

    // Otherwise attempt to allocate from an existing block before allocating new memory
    return
        allocateFromBlock(size)
        .or(() -> reallocate(size))
        .orElseGet(() -> allocateNewBlock(size));
}
```

Allocating a new block essentially delegates to the following helper:

```java
private Block block(long size) {
    // Allocate memory
    DeviceMemory mem;
    try {
        mem = allocator.allocate(type, size);
    }
    catch(Exception e) {
        throw new AllocationException(e);
    }
    if(mem == null) throw new AllocationException(...);

    // Add new block to the pool
    Block block = new Block(mem);
    blocks.add(block);
    total += actual;

    return block;
}
```

Memory can be allocated from an existing block with available free memory:

```java
private Optional<DeviceMemory> allocateFromBlock(long size) {
    return blocks
        .stream()
        .filter(b -> b.remaining() >= size)
        .findAny()
        .map(b -> b.allocate(size));
}
```

Or released memory can be reallocated if available:

```java
private Optional<DeviceMemory> reallocate(long size) {
    return blocks
        .stream()
        .flatMap(Block::allocations)
        .filter(DeviceMemory::isDestroyed)
        .filter(mem -> mem.size() >= size)
        .sorted(Comparator.comparingLong(DeviceMemory::size))
        .findAny()
        .map(BlockDeviceMemory::reallocate);
}
```

Note that although the pool supports pre-allocation of blocks and reallocation, the overall memory will be subject to fragmentation.  However we have decided that de-fragmentation is out-of-scope for a couple of reasons:

1. A de-fragmentation algorithm would be very complex to implement and test.

2. Applications would probably prefer pre-allocation and periodic pool flushes.

### Allocator

Next a second allocator implementation is created to manage a group of memory pools:

```java
public class PoolAllocator implements Allocator {
    private final Allocator allocator;
    private final Map<MemoryType, MemoryPool> pools = new ConcurrentHashMap<>();
    private final int max;
    private int count;
}
```

The `max` member is the total maximum number of allowed allocations (see below).

A pool is instantiated on demand for each memory type:

```java
public MemoryPool pool(MemoryType type) {
    return pools.computeIfAbsent(type, ignored -> new MemoryPool(type, allocator));
}
```

Which is invoked on a request for new device memory from the application:

```java
public DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
    // Allocate from pool
    MemoryPool pool = pool(type);
    DeviceMemory mem = pool.allocate(size);

    // Update stats
    ++count;

    return mem;
}
```

The allocator provides methods to release all allocated memory back to the pool:

```java
public void release() {
    pools.values().forEach(MemoryPool::release);
    assert free() == size();
}
```

And to destroy all allocated memory:

```java
public void destroy() {
    pools.values().forEach(MemoryPool::close);
    count = 0;
    assert size() == 0;
    assert free() == 0;
}
```

---

## Allocation Strategy

### Allocation Policy

The final abstraction is an _allocation policy_ which modifies memory requests to support the following requirements:

* Configuration of the growth policy for memory pools.

* Memory pagination (see below).

Allocation policies are defined by the following function:

```java
@FunctionalInterface
public interface AllocationPolicy {
    /**
     * Calculates the size of a new memory according to this policy.
     * @param size          Requested memory size
     * @param total         Total memory
     * @return Modified size to allocate
     */
    long apply(long size, long total);

    /**
     * Default policy that does not modify the requested size.
     */
    AllocationPolicy NONE = (size, total) -> size;
}
```

Various convenience implementations are provided, in particular the following factory creates a policy to grow a memory pool:

```java
static AllocationPolicy expand(float scale) {
    if(scale <= 0) throw new IllegalArgumentException(...);
    return (size, total) -> (long) (total * scale);
}
```

Allocation policies can also be chained:

```java
/**
 * Chains an allocation policy.
 * @param policy Policy to be applied <i>after</i> this policy
 * @return Chained policy
 */
default AllocationPolicy then(AllocationPolicy policy) {
    return (size, total) -> {
        final long actual = apply(size, total);
        return policy.apply(actual, total);
    };
}
```

The pool allocator is refactored to apply the allocation policy before delegating to the memory pool:

```java
public DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
    // Apply allocation policy
    MemoryPool pool = pool(type);
    long actual = policy.apply(size, pool.size());

    // Allocate from pool
    DeviceMemory mem = pool.allocate(Math.max(size, actual));
    ...
}
```

### Pagination

The `VkPhysicalDeviceLimits` descriptor contains two properties that can be used to configure memory allocation.

* `bufferImageGranularity` specifies the optimal _page size_ for memory blocks.

* `maxMemoryAllocationCount` is the maximum number of individual memory allocations that can be supported by the hardware.

A custom pagination policy quantises the memory request to a given page size:

```java
public class PageAllocationPolicy implements AllocationPolicy {
    private final long page;
    private final long min;

    @Override
    public long apply(long size, long current) {
        long num = 1 + ((size - 1) / page);
        return Math.max(min, num * page);
    }
}
```

A future chapter will introduce functionality to more easily configure components that are dependant on the device limits.

### Routing Policy

It is anticipated that an application will also require different allocation strategies depending on the use-cases for device memory.

For example a memory pool could be used for infrequent, one-off memory requests for transferring data to the hardware.
Whereas it may be more appropriate to pre-allocate a single, fixed memory instance for highly volatile memory such as a uniform buffer.

A sub-class of the allocation service is introduced with a _routing policy_ to support multiple use-cases:

```java
public class AllocationRoutingService extends AllocationService {
    /**
     * Route descriptor.
     */
    private record Route(Predicate<MemoryProperties<?>> predicate, Allocator allocator) {
    }

    private final List<Route> routes = new ArrayList<>();

    public void route(Predicate<MemoryProperties<?>> predicate, Allocator allocator) {
        routes.add(new Route(predicate, allocator));
    }
}
```

The appropriate `allocator` for a given memory request is then determined from the routing policy:

```java
@Override
protected Allocator allocator(MemoryProperties<?> props) {
    return routes
        .stream()
        .filter(r -> r.predicate().test(props))
        .findAny()
        .map(Route::allocator)
        .orElseGet(() -> super.allocator(props));
}
```

And the base-class is modified by the addition of the new `allocator` provider method.

Notes:

* The routing policy is mutable.

* Routes are implicitly ordered.

* This implementation delegates to the default allocator if there is no matching route.

---

## Summary

In this chapter the framework for memory allocation was implemented to support vertex buffers and texture images.

