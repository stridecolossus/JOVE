package org.sarge.jove.platform.vulkan.memory;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;

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

	private final Map<MemoryType, Pool> pools = new ConcurrentHashMap<>();
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
		return pools.values().stream().mapToLong(Pool::free).sum();
	}

	/**
	 * @return Total amount of allocated memory
	 */
	public long size() {
		return pools.values().stream().mapToLong(Pool::size).sum();
	}

	/**
	 * Retrieves or creates the pool for the given memory type.
	 * @param type Memory type
	 * @return Pool
	 */
	public Pool pool(MemoryType type) {
		return pools.computeIfAbsent(type, Pool::new);
	}

	/**
	 * @return Memory pools ordered by type
	 */
	public Map<MemoryType, Pool> pools() {
		return Map.copyOf(pools);
	}

	@Override
	public DeviceMemory allocate(MemoryType type, long size) throws AllocationException {
		if(count >= max) throw new AllocationException("Maximum number of allocations exceeded");
		final Pool pool = pool(type);
		return pool.allocate(size);
	}

	/**
	 * Releases <b>all</b> allocated memory back to the pool.
	 */
	public synchronized void release() {
		pools.values().forEach(Pool::release);
		count = 0;
		assert free() == size();
	}

	/**
	 * Destroys <b>all</b> memory allocation by this pool.
	 */
	public synchronized void close() {
		pools.values().forEach(Pool::close);
		count = 0;
		assert size() == 0;
		assert free() == 0;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("count", count)
				.append("max", max)
				.append(pools)
				.build();
	}

	/**
	 * A <i>block</i> is a chunk of memory managed by a pool.
	 */
	private static class Block {
		private final DeviceMemory mem;
		private final List<DeviceMemory> allocations = new ArrayList<>();
		private long free;

		private Block(DeviceMemory mem) {
			this.mem = mem;
		}

		/**
		 * @return Free memory in this block
		 */
		private long free() {
			final long total = allocations().mapToLong(DeviceMemory::size).sum();
			return mem.size() - total;
		}

		/**
		 * @return Remaining free memory in this block
		 */
		private long remaining() {
			return mem.size() - free;
		}

		/**
		 * @return Allocated memory in this block
		 */
		private Stream<DeviceMemory> allocations() {
			return allocations.stream().filter(Predicate.not(DeviceMemory::isDestroyed));
		}

		/**
		 * Allocates memory from this end of this block.
		 * @param size Memory size
		 * @return New memory allocation
		 */
		private DeviceMemory allocate(long size) {
			// Allocate from free space
			final DeviceMemory alloc = new BlockDeviceMemory(free, size);
			allocations.add(alloc);

			// Update free space pointer
			free += size;
			assert free <= mem.size();

			return alloc;
		}

		/**
		 * Device memory allocated from this block.
		 */
		private class BlockDeviceMemory implements DeviceMemory {
			private final long offset;
			private final long size;

			private boolean destroyed;

			/**
			 * Constructor.
			 * @param size
			 * @param offset
			 */
			private BlockDeviceMemory(long offset, long size) {
				this.offset = offset;
				this.size = size;
			}

			@Override
			public Handle handle() {
				return mem.handle();
			}

			@Override
			public long size() {
				return size;
			}

			@Override
			public Optional<Region> region() {
				return mem.region();
			}

			@Override
			public Region map(long offset, long size) {
				if(destroyed) throw new IllegalStateException("Device memory has been destroyed: " + this);
				unmap();
				return mem.map(offset, size);
			}

			@Override
			public boolean isDestroyed() {
				return destroyed || mem.isDestroyed();
			}

			@Override
			public synchronized void close() {
				if(isDestroyed()) throw new IllegalStateException("Device memory has already been destroyed: " + this);
				unmap();
				destroyed = true;
			}

			private void unmap() {
				mem.region().ifPresent(Region::unmap);
			}

			@Override
			public boolean equals(Object obj) {
				return
						(obj == this) ||
						(obj instanceof BlockDeviceMemory that) &&
						(this.size == that.size) &&
						(this.offset == that.offset) &&
						this.handle().equals(that.handle());
			}

			@Override
			public String toString() {
				return new ToStringBuilder(this)
						.append("offset", offset)
						.append("size", size)
						.append("mem", mem)
						.build();
			}
		}
	}

	/**
	 * A <i>pool</i> is a delegate manager for memory allocations of a given type.
	 * <p>
	 * The pool manages a number of <i>blocks</i> from which device memory is allocated growing the pool as required.
	 * Released memory allocations are restored to the pool and potentially reallocated.
	 * <p>
	 * The {@link Pool#init(long)} method can be used to pre-allocate memory into the pool.
	 */
	public class Pool {
		private final MemoryType type;
		private final List<Block> blocks = new ArrayList<>();
		private long total;

		private Pool(MemoryType type) {
			this.type = type;
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
		 * @return Memory allocations in this pool
		 */
		public Stream<DeviceMemory> allocations() {
			return blocks.stream().flatMap(Block::allocations);
		}

		/**
		 * Initialises this pool with the given amount of free memory.
		 * @param size Amount of memory to add to this pool
		 * @throws AllocationException if the memory cannot be allocated
		 */
		public void init(long size) {
			block(size);
			assert free() >= size;
		}

		/**
		 * Allocates memory from this pool.
		 * @param size  Memory size
		 * @return Allocated memory
		 */
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
					.flatMap(b -> b.allocations.stream())
					.filter(DeviceMemory::isDestroyed)
					.filter(mem -> mem.size() <= size)
					.sorted(Comparator.comparingLong(DeviceMemory::size)) // TODO - right way round?
					.findAny();

			// TODO - need destroyed = false surely?
		}

		/**
		 * Allocates a new memory block in this pool.
		 * @param size Block size
		 * @return New memory block
		 * @throws AllocationException if the underlying allocator failed
		 */
		private Block block(long size) {
			// Allocate memory
			final DeviceMemory mem;
			try {
				mem = allocator.allocate(type, size);
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
			++count;

			return block;
		}

		/**
		 * Releases <b>all</b> memory allocated back to this pool.
		 */
		public synchronized void release() {
			final var allocations = allocations().collect(toList());		// Copy to avoid concurrent modification
			allocations.forEach(DeviceMemory::close);
			assert free() == total;
		}

		/**
		 * Destroys <b>all</b> memory allocated by this pool.
		 */
		public synchronized void close() {
			// Remove allocations count
			count -= blocks.size();
			assert count >= 0;

			// Destroy allocated blocks
			for(Block b : blocks) {
				b.mem.close();
			}
			blocks.clear();
			total = 0;
			assert free() == 0;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("size", total)
					.append("free", free())
					.append("blocks", blocks.size())
					.build();
		}
	}
}
