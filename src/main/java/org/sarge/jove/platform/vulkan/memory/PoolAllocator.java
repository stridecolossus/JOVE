package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * A <i>pool allocator</i> delegates allocation requests to a {@link MemoryPool}.
 * <p>
 * This implementation creates a memory pool for <b>each</b> memory type on demand.
 * The pool grows as required according to the {@link AllocationPolicy} specified in the constructor.
 * <p>
 * @author Sarge
 */
public class PoolAllocator extends Allocator {
	private final Map<MemoryType, MemoryPool> pools = new ConcurrentHashMap<>();
	private final Allocator allocator;
	private final int max;
	private final AllocationPolicy policy;
	private int count;

	/**
	 * Constructor.
	 * @param allocator		Delegate allocator
	 * @param max			Maximum number of allocations
	 * @param policy		Allocation policy
	 */
	public PoolAllocator(Allocator allocator, int max, AllocationPolicy policy) {
		super(allocator);
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
		return pools.computeIfAbsent(type, MemoryPool::new);
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
		// TODO - seems nasty extending AND reference to delegate?
		final DeviceMemory mem = pool.allocate(Math.max(size, actual), allocator);

		// Update stats
		++count;

		return mem;
	}

	/**
	 * Releases <b>all</b> allocated memory back to the pool.
	 */
	public void release() {
		pools.values().forEach(MemoryPool::release);
		assert free() == size();
	}

	/**
	 * Destroys <b>all</b> memory allocation by this pool.
	 */
	public void destroy() {
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
