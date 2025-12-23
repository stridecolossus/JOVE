package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A <i>pool allocator</i> delegates allocation requests to a {@link MemoryPool}.
 * <p>
 * This implementation creates a memory pool for <b>each</b> memory type on demand which grows as required.
 * Memory can be pre-allocated using the {@link #add(MemoryType, long)} method.
 * <p>
 * @author Sarge
 */
public class PoolAllocator extends Allocator {
	private final Map<MemoryType, MemoryPool> pools = new ConcurrentHashMap<>();
	private final int pages;

	/**
	 * Constructor.
	 * @param allocator		Delegate allocator
	 * @param pages			Minimum number of pages per memory allocation
	 */
	public PoolAllocator(Allocator allocator, int pages) {
		super(allocator);
		this.pages = requireOneOrMore(pages);
	}

	/**
	 * @return Amount of free space
	 */
	public long free() {
		return pools
				.values()
				.stream()
				.mapToLong(MemoryPool::free)
				.sum();
	}

	/**
	 * @return Total amount of allocated memory
	 */
	public long size() {
		return pools
				.values()
				.stream()
				.mapToLong(MemoryPool::size)
				.sum();
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
		return Collections.unmodifiableMap(pools);
	}

	/**
	 * Pre-allocates free memory of the given type.
	 * @param type Memory type
	 * @param size Size
	 */
	public void add(MemoryType type, long size) {
		final DeviceMemory memory = super.allocate(type, size);
		final Block block = new Block(memory);
		final MemoryPool pool = pool(type);
		pool.add(block);
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
	 * @param size 		Size
	 * @param pool		Memory pool
	 * @return Allocated memory
	 * @throws AllocationException if a new block cannot be allocated
	 */
	private DeviceMemory create(MemoryType type, long size, MemoryPool pool) throws AllocationException {
		final DeviceMemory memory = super.allocate(type, size);
		final Block block = new Block(memory);
		pool.add(block);
		return block.allocate(size);
	}

	@Override
	protected long pages(long size) {
		return Math.max(pages, super.pages(size));
	}

	/**
	 * Releases <b>all</b> allocated memory back to the pool.
	 */
	@Override
	public void release() {
		for(MemoryPool p : pools.values()) {
			p.release();
		}

		assert free() == size();
	}

	@Override
	public void destroy() {
		super.destroy();

		for(MemoryPool p : pools.values()) {
			p.destroy();
		}

		assert size() == 0;
		assert free() == 0;
	}
}
