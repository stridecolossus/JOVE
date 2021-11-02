package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * A <i>pool allocator</i> maintains a <i>pool</i> of memory in order to reduce the total number of active allocations.
 * <p>
 * This implementation creates a pool for <b>each</b> memory type on demand which grows as required.
 * <br>
 * Memory can also be pre-allocated using the {@link Pool#init(long)} method.
 * <p>
 * Note that each pool contains a number of memory <i>blocks</i> from which individual instances are allocated.
 * <br>
 * Therefore a region mapping on <b>any</b> memory instance within a given block implicitly maps the <b>whole</b> block.
 * <br>
 * See {@link DeviceMemory#map(long, long)}.
 * <p>
 * Usage:
 * <pre>
 *  // Create allocator
 *  Allocator delegate = ...
 *  PoolAllocator allocator = new PoolAllocator(delegate, max);
 *
 *  // Initialise a pool
 *  MemoryType type = ...
 *  Pool pool = allocator.pool(type);
 *  pool.init(amount);
 *
 *  // Allocate some memory
 *  DeviceMemory mem = allocator.allocate(type, size);
 *
 *  // Release memory back to the pool
 *  mem.destroy();
 *
 *  ...
 *
 *  // Cleanup
 *  allocator.close();
 * </pre>
 * <p>
 * @see Allocator#paged(Allocator, long)
 * @author Sarge
 */
public class PoolAllocator implements Allocator {
	private final Allocator allocator;
	private final int max;

	private final Map<MemoryType, MemoryPool> pools = new ConcurrentHashMap<>();
	private int count;

	/**
	 * Constructor.
	 * @param allocator 	Underlying allocator
	 * @param max			Maximum number of allocations
	 */
	public PoolAllocator(Allocator allocator, int max) {
		this.allocator = notNull(allocator);
		this.max = oneOrMore(max);
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

		// Allocate from pool
		final MemoryPool pool = pool(type);
		final DeviceMemory mem = pool.allocate(size);

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
	public synchronized void close() {
		pools.values().forEach(MemoryPool::close);
		count = 0;
		assert size() == 0;
		assert free() == 0;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("allocations", String.format("%d/%d", count, max))
				.append("pools", pools.size())
				.build();
	}
}
