package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.memory.Allocator.AllocationException;
import org.sarge.jove.platform.vulkan.memory.Block.BlockDeviceMemory;
import org.sarge.jove.platform.vulkan.memory.DeviceMemory.Region;
import org.sarge.lib.util.Check;

/**
 * A <i>memory pool</i> is comprised of a number of <i>blocks</i> from which device memory is allocated.
 * The pool grows as required according to the configured {@link BlockPolicy}.
 * <p>
 * Released memory allocations are restored to the pool and potentially reallocated.
 * <p>
 * Free memory can be pre-allocated into the pool using the {@link Pool#init(long)} method.
 * <p>
 * Note that the mapped {@link Region} for a block can be silently unmapped by the pool since only one mapped region is permitted per block by the underlying implementation.
 * The client is responsible for ensuring that a new region is mapped as required.
 * Alternatively a non-pooled allocator implementation should be considered where memory mapping is highly volatile.
 * <p>
 * @author Sarge
 */
public class MemoryPool {
	private final MemoryType type;
	private final Allocator allocator;
	private final List<Block> blocks = new ArrayList<>();
	private BlockPolicy policy = BlockPolicy.NONE;
	private long total;

	/**
	 * Constructor.
	 * @param type				Memory type
	 * @param allocator			Allocator
	 */
	MemoryPool(MemoryType type, Allocator allocator) {
		this.type = notNull(type);
		this.allocator = notNull(allocator);
	}

	/**
	 * @return Amount of available memory in this pool
	 */
	public long free() {
		return blocks.stream().mapToLong(Block::free).sum();
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
	 * @param size Amount of memory to add to this pool
	 * @throws AllocationException if the memory cannot be allocated
	 * @see #policy(BlockPolicy)
	 */
	public void init(long size) {
		block(size);
		assert free() >= size;
	}

	/**
	 * Sets the growth policy for this memory pool (default is {Policy#NONE}).
	 * @param policy Growth policy
	 */
	public void policy(BlockPolicy policy) {
		this.policy = notNull(policy);
	}

	/**
	 * Allocates memory from this pool.
	 * @param size  Memory size
	 * @return Allocated memory
	 */
	synchronized DeviceMemory allocate(long size) {
		// Short cut to allocate a new block if pool has insufficient free memory
		if(free() < size) {
			return allocateNewBlock(size);
		}

		// Otherwise attempt to re/allocate from an existing block before allocating new memory
		return
				allocateFromBlock(size)
				.or(() -> reallocate(size))
				.orElseGet(() -> allocateNewBlock(size));
	}

	/**
	 * Allocates from a new block.
	 * @param size Allocation size
	 * @return Allocated memory
	 */
	private DeviceMemory allocateNewBlock(long size) {
		final Block block = block(size);
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
				.filter(mem -> mem.size() <= size)
				.sorted(Comparator.comparingLong(DeviceMemory::size))
				.findAny()
				.map(BlockDeviceMemory::reallocate);
	}

	/**
	 * Allocates a new memory block in this pool.
	 * @param size Block size
	 * @return New memory block
	 * @throws AllocationException if the underlying allocator failed
	 */
	private Block block(long size) {
		// Apply growth policy
		Check.oneOrMore(size);
		final long mod = policy.apply(size, total);
		if(mod < size) throw new AllocationException(String.format("Invalid block size policy: policy=%s size=%d", policy, size));

		// Allocate memory
		final DeviceMemory mem;
		try {
			mem = allocator.allocate(type, mod);
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
	public synchronized void release() {
		final var allocations = this.allocations();
		allocations.forEach(DeviceMemory::close);
		assert free() == total;
	}

	/**
	 * Destroys <b>all</b> memory allocated by this pool.
	 */
	public synchronized void close() {
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
				.append("policy", policy)
				.append("size", total)
				.append("free", free())
				.append("blocks", blocks.size())
				.build();
	}
}
