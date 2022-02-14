---
title: Memory Allocation
---

---

## Contents

- [Overview](#overview)
- [Device Memory](#device-memory)
- [Memory Pool](#memory-pool)
- [Enhancements](#enhancements)

---

## Overview

In the following chapters we will be implementing vertex buffers and textures, both of which are dependant on _device memory_ allocated by Vulkan.  Device memory resides on the host (visible to the application and the GPU) or on the graphics hardware (visible only to the GPU).  A Vulkan implementation specifies a set of _memory types_ such that the application can select the appropriate memory type depending on the usage scenario.

The [Vulkan documentation](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html) suggests implementing a _fallback strategy_ when selecting the memory type.  The application requests _optimal_ and _minimal_ properties for the required memory, with the algorithm falling back to the minimal properties if the optimal memory type is not available.

Additionally the maximum number of memory allocations supported by the hardware is limited.  A real-world application would allocate a memory _pool_ and then serve allocation requests as offsets into that pool (growing the available memory as required).  Initially we will allocate a new memory block for every request and then extend the implementation to use a memory pool.

References:

- [VkPhysicalDeviceMemoryProperties](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/VkPhysicalDeviceMemoryProperties.html)
- [Custom Allocators (Blog)](http://kylehalladay.com/blog/tutorial/2017/12/13/Custom-Allocators-Vulkan.html)

---

## Device Memory

### Definition

We start with an abstraction for device memory:

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

We create a new domain object for the default implementation:

```java
public class DefaultDeviceMemory extends AbstractVulkanObject implements DeviceMemory {
    private final long size;

    public DefaultDeviceMemory(Pointer handle, DeviceContext dev, long size) {
        super(handle, dev);
        this.len = oneOrMore(size);
    }

    @Override
    public long size() {
        return size;
    }

    @Override
    protected Destructor<DefaultDeviceMemory> destructor(VulkanLibrary lib) {
        return lib::vkFreeMemory;
    }
}
```

And an inner class for the implementation of the mapped region:

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
    DeviceContext dev = device();
    VulkanLibrary lib = dev.library();
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

Finally we implement a _memory properties_ record:

```java
public record MemoryProperties<T>(Set<T> usage, VkSharingMode mode, Set<VkMemoryProperty> required, Set<VkMemoryProperty> optimal) {
    public static class Builder {
        ...
    }
}
```

This new type specifies the intent of the memory and the _optimal_ and _required_ properties for the request (used in memory selection below).

Note that the record is generic based on the relevant usage enumeration, e.g. `VkImageUsage` for device memory used by an image.

### Memory Selection

The Vulkan documentation outlines the suggested approach for selecting the appropriate memory type for a given allocation request as follows:

1. Walk through the supported memory types.

2. Filter by _index_ using the _filter_ bit-mask from the `VkMemoryRequirements` of the object in question.

3. Filter by matching the supported memory properties against the _optimal_ properties of the request.

4. Or fall back to the _required_ properties.

The allocation algorithm is encapsulated in a new component:

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
public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
    List<MemoryType> candidates = IntStream
        .range(0, types.length)
        .filter(n -> MathsUtil.isBit(reqs.memoryTypeBits, n))
        .mapToObj(n -> types[n])
        .collect(toList());

    ...
}
```

Where `MathsUtil` is a new utility class for common arithmetic functions, the following methods are used to test whether a given memory type bit is set in the supplied bit-field filter:

```java
public static boolean isBit(int mask, int bit) {
    return isMask(1 << bit, mask);
}

public static boolean isMask(int value, int mask) {
    return (value & mask) == value;
}
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
    if(props.isEmpty()) {
        return Optional.empty();
    }

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
        final PointerByReference ref = dev.factory().pointer();
        check(lib.vkAllocateMemory(dev, info, null, ref));

        // Create memory wrapper
        return new DefaultDeviceMemory(ref.getValue(), dev, size);
    };
}
```

Note that the actual size of the allocated memory may be larger than the requested length depending on how the hardware handles alignment.

All the above components are brought together into the following service which is the entry-point for allocation of device memory:

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

With a basic memory framework in place we can now implement a memory pool.

Our requirements are:

- One pool per memory type.

- A pool grows as required.

- Memory can be pre-allocated to suit the application (to reduce the number of allocations and avoid fragmentation).

- Memory allocated by the pool can be destroyed by the application and potentially re-allocated.

- The allocator and pool(s) should provide useful statistics to the application, e.g. number of allocations, free memory, etc.

To support these requirements we will introduce a second device memory implementation for a _block_ of memory managed by the pool.  Device memory is then allocated from a block with available free memory or new blocks are created on demand to grow the pool.

### Memory Blocks

A memory block is a wrapper for an area of memory and maintains the list of allocations from the block:

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

Memory is allocated from the end of the block indicated by the `next` free-space offset:

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
    public synchronized void destroy() {
        checkAlive();
        destroyed = true;
    }

    private void checkAlive() {
        if(isDestroyed()) throw new IllegalStateException(...);
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
        checkAlive();
        mem.region().ifPresent(Region::unmap);
        return mem.map(offset, size);
    }

    @Override
    public boolean isDestroyed() {
        return destroyed || mem.isDestroyed();
    }
}
```

Note that since only one region mapping is allowed per memory block the `map` method silently releases the previous mapping (if any).

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
static final Predicate<BlockDeviceMemory> ALIVE = Predicate.not(DeviceMemory::isDestroyed);
```

### Pool

Each memory pool is comprised of a number of blocks from which memory requests are served:

```java
public class MemoryPool {
    private final MemoryType type;
    private final Allocator allocator;
    private final List<Block> blocks = new ArrayList<>();
    private long total;

    synchronized DeviceMemory allocate(long size) {
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
public synchronized void release() {
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
public synchronized void destroy() {
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

2. Find a released allocation that can be re-allocated.

3. Otherwise allocate a new block.

This is implemented as follows:

```java
private synchronized DeviceMemory allocate(long size) {
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
        .filter(mem -> mem.size() <= size)
        .sorted(Comparator.comparingLong(DeviceMemory::size))
        .findAny()
        .map(BlockDeviceMemory::reallocate);
}
```

Note that although the pool supports pre-allocation of blocks and reallocation, the overall memory will be subject to fragmentation.  However we have decided that de-fragmentation is out-of-scope for a couple of reasons:

1. A de-fragmentation algorithm would be very complex to implement and test.

2. Applications would probably prefer pre-allocation and periodic pool flushes.

### Allocator

We next create a new allocator implementation to manage a group of memory pools:

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
public synchronized void release() {
    pools.values().forEach(MemoryPool::release);
    assert free() == size();
}
```

And to destroy all allocated memory:

```java
public synchronized void destroy() {
    pools.values().forEach(MemoryPool::close);
    count = 0;
    assert size() == 0;
    assert free() == 0;
}
```

Finally we provide accessors for the various pool properties:

```java
public int count() {
    return count;
}

public long free() {
    return pools.values().stream().mapToLong(Pool::free).sum();
}

public long size() {
    return pools.values().stream().mapToLong(Pool::size).sum();
}
```

---

## Enhancements

### Allocation Policy

We next introduce an _allocation policy_ which modifies the size of a memory request to support the following requirements:

* Configuration of the growth policy for memory pools.

* Memory pagination (see below).

An allocation policy is defined by the following function:

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

We provide various implementations, in particular the following factory creates a policy to grow a memory pool:

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

An allocation policy is added to the pool allocator which is applied before delegating to the memory pool:

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

We first implement a custom pagination policy which quantises the memory request to a given page size:

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

Next we implement a convenience factory to create and configure a pool allocator based on the hardware:

```java
public static PoolAllocator create(LogicalDevice dev, Allocator allocator, float expand) {
    // Init allocator if not specified
    Allocator delegate = allocator == null ? Allocator.allocator(dev) : allocator;

    // Create paged allocation policy
    VkPhysicalDeviceLimits limits = dev.parent().properties().limits();
    AllocationPolicy paged = new PageAllocationPolicy(limits.bufferImageGranularity);
    AllocationPolicy grow = AllocationPolicy.expand(expand);
    AllocationPolicy policy = grow.then(paged);

    // Create pool allocator
    return new PoolAllocator(delegate, limits.maxMemoryAllocationCount, policy);
}
```

The `VkPhysicalDeviceLimits` structure is a child of `VkPhysicalDeviceProperties` wrapped by the `Properties` helper class of the physical device:

```java
public class PhysicalDevice ... {
    public class Properties {
        private final VkPhysicalDeviceProperties struct = new VkPhysicalDeviceProperties();
        ...

        public VkPhysicalDeviceLimits limits() {
            return struct.limits;
        }
    }
}
```

However this implementation directly exposes the limits which as a JNA structure is a mutable object, potentially allowing an application to accidentally monkey with the data.  Ideally we would also wrap this information in another immutable domain object, however this structure is absolutely huge and the wrapper approach is simply not viable.  Unfortunately neither is there an obvious means of making a JNA structure immutable.

Instead we introduce a mechanism to _clone_ a JNA structure by copying the underlying memory:

```java
public abstract class VulkanStructure extends Structure {
    public <T extends VulkanStructure> T copy() {
        // Create copy
        T copy = (T) Structure.newInstance(this.getClass());
        write();

        // Read backing data
        int size = this.size();
        byte[] data = getPointer().getByteArray(0, size);

        // Write to copy
        copy.getPointer().write(0, data, 0, size);
        copy.read();

        return copy;
    }
}
```

The accessor for the device `limits` is modified accordingly to clone on demand:

```java
public VkPhysicalDeviceLimits limits() {
    return struct.limits.copy();
}
```

Obviously this is not an ideal solution since the developer needs to be aware that the accessor clones a new copy on every invocation.

### Allocation Request Routing

We also anticipate that an application will require different allocation strategies depending on the use-cases for device memory.

For example:

* Applications would probably prefer to avoid frequent mapping of memory that is highly volatile, e.g. a uniform buffer for projection matrices.

* Transient memory is generally explicitly released by an application, e.g. staging buffers.

* A memory pool is generally suitable for similar or frequent types of request (e.g. an image processing application) whereas one-off allocations can probably be arbitrarily satisfied.

To support these different use-cases we introduce a _routing policy_ to the allocation service:

```java
public class AllocationService {
    /**
     * Route descriptor.
     */
    private record Route(Predicate<MemoryProperties<?>> predicate, Allocator allocator) {
    }

    private final List<Route> routes = new ArrayList<>();
    private final Allocator def;
    ...

    public void route(Predicate<MemoryProperties<?>> predicate, Allocator allocator) {
        routes.add(new Route(predicate, allocator));
    }
}
```

The `allocate` method is modified to find the matching route based on the memory properties of the request:

```java
public DeviceMemory allocate(VkMemoryRequirements reqs, MemoryProperties<?> props) throws AllocationException {
    // Route request
    Allocator allocator = routes
        .stream()
        .filter(r -> r.predicate().test(props))
        .findAny()
        .map(r -> r.allocator())
        .orElse(def);

    // Select memory type and delegate request
    MemoryType type = selector.select(reqs, props);
    return allocator.allocate(type, reqs.size);
}
```

Notes:

* The routing policy is mutable.

* Routes are implicitly ordered.

* The allocator in the constructor is the default if there is no matching route (or no routes are configured).

---

## Summary

In this chapter we implemented the framework for memory allocation that will be used when creating vertex buffers and texture images.

