package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice;
import org.sarge.lib.util.Check;

/**
 * A <i>pool allocator</i> delegates allocation requests to a {@link MemoryPool}.
 * <p>
 * This implementation creates a memory pool for <b>each</b> memory type on demand.
 * The pool grows as required according to a {@link AllocationPolicy} configured using the {@link #policy(AllocationPolicy)} method.
 * <p>
 * @author Sarge
 */
public class PoolAllocator implements Allocator {
	/**
	 * Helper - Creates a pool allocator configured by the properties of the given device.
	 * <p>
	 * The pool is configured as follows:
	 * <ul>
	 * <li>{@link VkPhysicalDeviceLimits#bufferImageGranularity} specifies the <i>page size</i> used to create a {@link PageAllocationPolicy} for the allocator</li>
	 * <li>{@link VkPhysicalDeviceLimits#maxMemoryAllocationCount} configures the maximum number of allowed allocations</li>
	 * <li>if {@link #allocator} is {@code null} a default allocator is created using {@link Allocator#allocator(DeviceContext)}</li>
	 * </ul>
	 * <p>
	 * @param dev			Logical device
	 * @param allocator		Optional delegate allocator
	 * @param expand		Growth scalar
	 * @return New pool allocator
	 * @see PhysicalDevice.Properties#limits()
	 */
	public static PoolAllocator create(LogicalDevice dev, Allocator allocator, float expand) {
		// Init allocator if not specified
		final Allocator delegate = allocator == null ? Allocator.allocator(dev) : allocator;

		// Create paged allocation policy
		final VkPhysicalDeviceLimits limits = dev.parent().properties().limits();
		final AllocationPolicy paged = new PageAllocationPolicy(limits.bufferImageGranularity);
		final AllocationPolicy grow = AllocationPolicy.expand(expand);
		final AllocationPolicy policy = grow.then(paged);

		// Create pool allocator
		return new PoolAllocator(delegate, limits.maxMemoryAllocationCount, policy);
	}

	private final Allocator allocator;
	private final Map<MemoryType, MemoryPool> pools = new ConcurrentHashMap<>();
	private final AllocationPolicy policy;
	private final int max;
	private int count;

	/**
	 * Constructor.
	 * @param allocator 	Underlying allocator
	 * @param max			Maximum number of allocations
	 * @param policy		Allocation policy
	 */
	public PoolAllocator(Allocator allocator, int max, AllocationPolicy policy) {
		this.allocator = notNull(allocator);
		this.max = oneOrMore(max);
		this.policy = notNull(policy);
	}

	/**
	 * @return Number of memory allocations
	 */
	public int count() {
		return count;
	}

	/**
	 * @return Amount of free space
	 */
	public long free() {
		return pools.values().stream().mapToLong(MemoryPool::free).sum();
	}

	/**
	 * @return Total amount of allocated memory
	 */
	public long size() {
		return pools.values().stream().mapToLong(MemoryPool::size).sum();
	}

	/**
	 * Retrieves or creates the pool for the given memory type.
	 * @param type Memory type
	 * @return Pool
	 */
	public MemoryPool pool(MemoryType type) {
		return pools.computeIfAbsent(type, ignored -> new MemoryPool(type, allocator));
	}

	/**
	 * @return Memory pools ordered by type
	 */
	public Map<MemoryType, MemoryPool> pools() {
		return Map.copyOf(pools);
	}

	@Override
	public DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
		// Validate
		Check.oneOrMore(size);
		if(count >= max) throw new AllocationException("Maximum number of allocations exceeded");

		// Apply allocation policy
		final MemoryPool pool = pool(type);
		final long actual = policy.apply(size, pool.size());

		// Allocate from pool
		final DeviceMemory mem = pool.allocate(Math.max(size, actual));

		// Update stats
		++count;

		return mem;
	}

	/**
	 * Releases <b>all</b> allocated memory back to the pool.
	 */
	public synchronized void release() {
		pools.values().forEach(MemoryPool::release);
		assert free() == size();
	}

	/**
	 * Destroys <b>all</b> memory allocation by this pool.
	 */
	public synchronized void destroy() {
		pools.values().forEach(MemoryPool::destroy);
		count = 0;
		assert size() == 0;
		assert free() == 0;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("pools", pools.size())
				.append("allocations", String.format("%d/%d", count, max))
				.append("policy", policy)
				.build();
	}
}
