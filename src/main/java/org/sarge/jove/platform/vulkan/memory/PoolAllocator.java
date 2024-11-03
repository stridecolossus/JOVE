package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.Validation.requireOneOrMore;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.sarge.jove.common.TransientObject;

/**
 * A <i>pool allocator</i> delegates allocation requests to a {@link MemoryPool}.
 * <p>
 * This implementation creates a memory pool for <b>each</b> memory type on demand which grows as required.
 * <p>
 * @author Sarge
 */
public class PoolAllocator extends Allocator implements TransientObject {
	private final Map<MemoryType, MemoryPool> pools = new ConcurrentHashMap<>();
	private final int pages;

	/**
	 * Constructor.
	 * @param allocator		Delegate allocator
	 * @param pages			Number of pages to allocate for a new block
	 * @see #granularity()
	 */
	public PoolAllocator(Allocator allocator, int pages) {
		super(allocator);
		this.pages = requireOneOrMore(pages);
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
	protected DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
		final MemoryPool pool = pool(type);
		return pool
				.allocate(size)
				.or(() -> pool.reallocate(size))
				.orElseGet(() -> create(type, size, pool));
	}

	/**
	 * Allocates memory from a new block.
	 * @param type 		Memory type
	 * @param size 		Size (bytes)
	 * @param pool		Memory pool
	 * @return Allocated memory
	 * @throws AllocationException if a new block cannot be allocated
	 */
	private DeviceMemory create(MemoryType type, long size, MemoryPool pool) throws AllocationException {
		// Allocate memory for a new block
		final DeviceMemory mem = super.allocate(type, size);

		// Add new block to pool
		final Block block = new Block(mem);
		pool.add(block);

		// Allocate from this block
		return block.allocate(size);
	}

	@Override
	protected long pages(long size) {
		return Math.max(pages, super.pages(size));
	}

	/**
	 * Releases <b>all</b> allocated memory back to the pool.
	 */
	public void release() {
		pools.values().forEach(MemoryPool::release);
		assert free() == size();
	}

	@Override
	public void destroy() {
		pools.values().forEach(MemoryPool::destroy);
		super.reset();
		assert size() == 0;
		assert free() == 0;
	}
}
