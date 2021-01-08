package org.sarge.jove.common;

import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.util.Check;

/**
 * TODO
 * @author Sarge
 */
public interface DeviceMemory extends TransientNativeObject {
	/**
	 * @return Memory offset
	 */
	long offset();

	/**
	 * @return Size of this memory
	 */
	long size();

	/**
	 * Template implementation.
	 */
	abstract class AbstractDeviceMemory extends AbstractTransientNativeObject implements DeviceMemory {
		private final long size;
		private final long offset;

		/**
		 * Constructor.
		 * @param handle		Memory handle
		 * @param size			Memory size
		 * @param offset		Block offset
		 */
		protected AbstractDeviceMemory(Handle handle, long size, long offset) {
			super(handle);
			this.size = oneOrMore(size);
			this.offset = zeroOrMore(offset);
		}

		@Override
		public long offset() {
			return offset;
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public int hashCode() {
			return Objects.hash(handle(), size, offset);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj instanceof AbstractDeviceMemory that) &&
					(this.size == that.size) &&
					(this.offset == that.offset) &&
					this.handle().equals(that.handle());
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("handle", handle())
					.append("offset", offset)
					.append("size", size)
					.build();
		}
	}

	/**
	 * A <i>memory pool</i> is used to manage a set of memory allocations.
	 * <p>
	 * Usage:
	 * <pre>
	 *  // Create memory pool
	 *  Allocator allocator = ...
	 *  Pool pool = new Pool(allocator);
	 *
	 *  // Allocate memory from the pool
	 *  Memory mem = pool.allocate(size);
	 *
	 *  // Release memory back to the pool
	 *  mem.destroy();
	 * </pre>
	 */
	class Pool {
		/**
		 * Exception indicating that a memory allocation cannot be fulfilled.
		 */
		public static class AllocationException extends RuntimeException {
			/**
			 * Constructor.
			 * @param message Error message
			 */
			public AllocationException(String message) {
				super(message);
			}
		}

		/**
		 * A <i>memory allocator</i> allocates new memory blocks on demand.
		 */
		@FunctionalInterface
		public interface Allocator {
			/**
			 * Allocates a new block of memory.
			 * The {@code offset} is ignored.
			 * @param size Memory size
			 * @return Pointer to the new memory
			 * @throws AllocationException if the memory cannot be allocated
			 */
			DeviceMemory allocate(long size) throws AllocationException;

			/**
			 * Creates an adapter that allocates memory blocks of the given page size.
			 * @param page				Page size
			 * @param allocator			Underlying allocator
			 * @return Page allocator
			 */
			static Allocator paged(long page, Allocator allocator) {
				Check.oneOrMore(page);
				Check.notNull(allocator);

				return size -> {
					final long actual = ((size / page) + 1) * page;
					return allocator.allocate(actual);
				};
			}
		}

		/**
		 * A <i>memory block</i> is a chunk of memory in this pool.
		 */
		private class Block {
			private final DeviceMemory block;
			private final List<BlockMemory> allocations = new ArrayList<>();
			private long next;

			/**
			 * Constructor.
			 * @param block Memory block
			 */
			private Block(DeviceMemory block) {
				this.block = notNull(block);
			}

			/**
			 * @return Memory allocations in this block
			 */
			private Stream<BlockMemory> stream() {
				return allocations.stream();
			}

			/**
			 * @param size Required memory
			 * @return Whether this block can allocate the required memory
			 */
			private boolean isAvailable(long size) {
				return next + size <= block.size();
			}

			/**
			 * Allocates new memory from this block.
			 * @param size Memory size
			 * @return New memory
			 */
			private DeviceMemory allocate(long size) {
				assert isAvailable(size);

				// Allocate from available memory
				final BlockMemory mem = new BlockMemory(size, next);
				allocations.add(mem);

				// Increment next free offset
				next += size;
				assert next <= block.size();

				// Update memory stats
				free -= size;
				++count;

				return mem;
			}

			/*
			// TODO
			private void compact() {
				//final long free = free();
				final List<BlockMemory> compacted = new ArrayList<>(allocations.size());
				Long offset = null;
				for(final BlockMemory mem : allocations) {
					if(mem.isDestroyed()) {
						if(offset == null) {
							offset = mem.offset;
						}
					}
					else {
						if(offset != null) {
							final BlockMemory compact = new BlockMemory(mem.offset - offset, offset);
							compacted.add(compact);
							offset = null;
						}
						compacted.add(mem);
					}
				}

				if(offset != null) {
					//compacted.add(prev);
				}

				allocations.clear();
				allocations.addAll(compacted);

				// TODO - update 'next'
			}
			*/

			/**
			 * Memory allocated from this block.
			 */
			private class BlockMemory extends AbstractDeviceMemory {
				/**
				 * Constructor.
				 * @param size
				 * @param offset
				 */
				private BlockMemory(long size, long offset) {
					super(block.handle(), size, offset);
				}

				@Override
				protected void release() {
					free += size();
					--count;
					assert free <= total;
					assert count >= 0;
				}

				/**
				 * Re-allocates this memory.
				 */
				private BlockMemory reallocate() {
					// Re-allocate memory
					restore();

					// Update memory stats
					++count;
					free -= size();
					assert free >= 0;

					return this;
				}
			}
		}

		private final Allocator allocator;
		private final List<Block> blocks = new ArrayList<>();

		private long total;
		private long free;
		private int count;

		/**
		 * Constructor.
		 * @param allocator Memory allocator
		 */
		public Pool(Allocator allocator) {
			this.allocator = notNull(allocator);
		}

		/**
		 * @return Current total amount of memory allocated by this pool
		 */
		public long size() {
			return total;
		}

		/**
		 * @return Available memory in this pool
		 */
		public long free() {
			return free;
		}

		/**
		 * @return Number of allocations
		 */
		public int count() {
			return count;
		}

		/**
		 * @return Memory allocated from this pool
		 */
		public Stream<? extends DeviceMemory> allocations() {
			return blocks.stream().flatMap(Block::stream).filter(Predicate.not(DeviceMemory::isDestroyed));
		}

		/**
		 * Creates a new memory block.
		 * @param size Memory block size
		 * @return New memory block
		 */
		private Block create(long size) {
			// Allocate new memory block
			final DeviceMemory mem = allocator.allocate(size);
			if(mem == null) throw new AllocationException("Allocator returned null memory");
			if(mem.size() < size) throw new AllocationException("Allocator returned invalid memory: " + mem);

			// Create block
			final Block block = new Block(mem);
			blocks.add(block);
			total += mem.size();
			free += mem.size();

			return block;
		}

		/**
		 * Adds free memory to this pool.
		 * @param size Memory to add
		 * @throws IllegalArgumentException if the given size is zero-or-less
		 * @throws AllocationException if the memory cannot be allocated
		 */
		public synchronized void add(long size) throws AllocationException {
			Check.oneOrMore(size);
			create(size);
		}

		/**
		 * Allocates memory from this pool.
		 * @param size Amount of memory to allocate
		 * @return New memory
		 * @throws IllegalArgumentException if the given size is zero-or-less
		 * @throws AllocationException if the memory cannot be allocated by this pool
		 */
		public synchronized DeviceMemory allocate(long size) throws AllocationException {
			if(size < 1) throw new IllegalArgumentException("Invalid memory size: " + size);

			// Create new block if the pool is exhausted
			if(size > free) {
				return allocateNew(size);
			}

			// Otherwise try to allocate from existing block
			return
					reallocate(size)
					.or(() -> allocateExisting(size))
					.orElseGet(() -> allocateNew(size));
		}

		/**
		 * Allocates a new memory block.
		 * @param size Memory size
		 * @return Memory allocated from a new block
		 */
		private DeviceMemory allocateNew(long size) {
			final Block block = create(size);
			return block.allocate(size);
		}

		/**
		 * Re-allocates memory from a previously released block.
		 * @param size Memory size
		 * @return Re-allocated memory
		 */
		private Optional<DeviceMemory> reallocate(long size) {
			return blocks
					.stream()
					.flatMap(Block::stream)
					.filter(DeviceMemory::isDestroyed)
					.filter(mem -> mem.size() >= size)
					.findAny()
					.map(mem -> mem.reallocate());
			// TODO - sort to prefer exact size? limit(n).sorted(lowest size)
		}

		/**
		 * Allocates memory from an existing block.
		 * @param size Memory size
		 * @return New memory
		 */
		private Optional<DeviceMemory> allocateExisting(long size) {
			return blocks
					.stream()
					.filter(block -> block.isAvailable(size))
					.findAny()
					.map(block -> block.allocate(size));
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("size", total)
					.append("free", free)
					.append("blocks", blocks.size())
					.append("count", count)
					.build();
		}
	}
}