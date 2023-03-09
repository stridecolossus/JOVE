package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;

/**
 * A <i>memory pool</i> is comprised of a number of <i>blocks</i> from which device memory is allocated.
 * <p>
 * Notes:
 * <ul>
 * <li>The pool grows as required according to the configured {@link AllocationPolicy}</li>
 * <li>Released memory allocations are restored to the pool and potentially reallocated</li>
 * <li>Free memory can be pre-allocated into the pool via the {@link #init(long)} method</li>
 * </ul>
 * <p>
 * Note that a mapped {@link Region} for a block can be silently unmapped by the pool since only one mapped region is permitted per block.
 * The client is responsible for ensuring that a new region is mapped as required.
 * Alternatively a non-pooled allocator implementation could be considered where memory mapping is highly volatile.
 * <p>
 * @author Sarge
 */
public class MemoryPool implements TransientObject {
	private final MemoryType type;
	private final List<Block> blocks = new ArrayList<>();
	private long total;

	/**
	 * Constructor.
	 * @param type Memory type for this pool
	 */
	public MemoryPool(MemoryType type) {
		this.type = notNull(type);
	}

	/**
	 * @return Amount of available memory in this pool
	 */
	public long free() {
		return blocks
				.stream()
				.mapToLong(Block::free)
				.sum();
	}

	/**
	 * @return Total amount of memory in this pool
	 */
	public long size() {
		return total;
	}

	/**
	 * @return Number of memory blocks in this pool
	 */
	public int count() {
		return blocks.size();
	}

	/**
	 * @return Memory allocations in this pool
	 */
	public Stream<? extends DeviceMemory> allocations() {
		return blocks
				.stream()
				.flatMap(Block::allocations)
				.filter(Block.ALIVE);
	}

	/**
	 * Initialises this pool with the given amount of free memory.
	 * Note that the configured growth policy is applied to the given memory size.
	 * @param size 			Amount of memory to add to this pool (bytes)
	 * @param allocator		Memory allocator
	 * @throws AllocationException if the memory cannot be allocated
	 */
	public void init(long size, Allocator allocator) {
		// TODO - Ideally we should apply policy here as well
		block(size, allocator);
		assert free() >= size;
	}

	/**
	 * Allocates memory from this pool.
	 * @param size  		Required memory size (bytes)
	 * @param allocator		Memory allocator
	 * @return Allocated memory
	 */
	public DeviceMemory allocate(long size, Allocator allocator) {
		// Short cut to allocate a new block if pool has insufficient free memory
		if(free() < size) {
			return allocateNewBlock(size, allocator);
		}

		// Otherwise attempt to re/allocate from an existing block before allocating new memory
		return
				allocateFromBlock(size)
				.or(() -> reallocate(size))
				.orElseGet(() -> allocateNewBlock(size, allocator));
	}

	/**
	 * Allocates from a new block.
	 * @param size 			Allocation size
	 * @param allocator		Memory allocator
	 * @return Allocated memory
	 */
	private DeviceMemory allocateNewBlock(long size, Allocator allocator) {
		final Block block = block(size, allocator);
		return block.allocate(size);
	}

	/**
	 * Allocates from an existing block.
	 * @param size Allocation size
	 * @return Existing memory allocation
	 */
	private Optional<DeviceMemory> allocateFromBlock(long size) {
		return blocks
				.stream()
				.filter(b -> b.remaining() >= size)
				.findAny()
				.map(b -> b.allocate(size));
	}

	/**
	 * Re-allocates an existing memory allocation that has been restored to the pool.
	 * @param size Allocation size
	 * @return Re-allocated memory
	 */
	private Optional<DeviceMemory> reallocate(long size) {
		return blocks
				.stream()
				.flatMap(Block::allocations)
				.filter(DeviceMemory::isDestroyed)
				.filter(mem -> mem.size() >= size)
				.sorted(Comparator.comparingLong(DeviceMemory::size))
				.findAny()
				.map(DeviceMemory::reallocate);
	}

	/**
	 * Allocates a new memory block in this pool.
	 * @param size 			Block size
	 * @param allocator		Memory allocator
	 * @return New memory block
	 * @throws AllocationException if the underlying allocator failed
	 */
	private Block block(long size, Allocator allocator) {
		// Allocate memory
		final DeviceMemory mem;
		try {
			mem = allocator.allocate(type, size);
		}
		catch(AllocationException e) {
			throw e;
		}
		catch(Exception e) {
			throw new AllocationException(e);
		}
		if(mem == null) throw new AllocationException("Allocator returned NULL memory block");

		// Check memory
		final long actual = mem.size();
		if(actual < size) throw new AllocationException("Allocator returned memory block smaller than the requested size");

		// Add new block to the pool
		final Block block = new Block(mem);
		blocks.add(block);
		total += actual;

		return block;
	}

	/**
	 * Releases <b>all</b> memory allocated back to this pool.
	 */
	public void release() {
		final var allocations = this.allocations();
		allocations.forEach(DeviceMemory::destroy);
		assert free() == total;
	}

	@Override
	public void destroy() {
		for(Block b : blocks) {
			b.destroy();
		}
		blocks.clear();
		total = 0;
		assert free() == 0;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("type", type)
				.append("size", total)
				.append("free", free())
				.append("blocks", blocks.size())
				.build();
	}
}
