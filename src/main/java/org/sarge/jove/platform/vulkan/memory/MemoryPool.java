package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.memory.Block.BlockDeviceMemory;

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
	public final long free() {
		return blocks.stream().mapToLong(Block::free).sum();
	}

	/**
	 * @return Total amount of memory in this pool
	 */
	public final long size() {
		return total;
	}

	/**
	 * @return Number of memory blocks in this pool
	 */
	public final int count() {
		return blocks.size();
	}

	/**
	 * @return Memory allocations in this pool
	 */
	public final Stream<? extends DeviceMemory> allocations() {
		return blocks
				.stream()
				.flatMap(Block::allocations)
				.filter(Block.ALIVE);
	}
	// TODO - public?

	/**
	 * Adds a memory block to this pool.
	 * @param block Block to add
	 * @throws IllegalArgumentException if the block has already been added or is in use
	 */
	void add(Block block) {
		if(block.free() != block.size()) throw new IllegalArgumentException("Cannot add a block in use: " + block);
		if(blocks.contains(block)) throw new IllegalArgumentException("Block already added: " + block);
		blocks.add(block);
		total += block.size();
	}

	/**
	 * Allocates from an existing block with sufficient free memory.
	 * @param size Allocation size
	 * @return Existing memory allocation
	 */
	public Optional<DeviceMemory> allocate(long size) {
		return blocks
				.stream()
				.filter(block -> block.remaining() >= size)
				.findAny()
				.map(block -> block.allocate(size));
	}

	/**
	 * Reallocates existing memory that has been destroyed.
	 * @param size Allocation size
	 * @return Reallocated memory
	 * @see BlockDeviceMemory#reallocate(long)
	 */
	public Optional<DeviceMemory> reallocate(long size) {
		return blocks
				.stream()
				.flatMap(Block::allocations)
				.filter(DeviceMemory::isDestroyed)
				.filter(mem -> mem.size() >= size)
				.sorted(Comparator.comparingLong(DeviceMemory::size))
				.findAny()
				.map(mem -> mem.reallocate(size));
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
