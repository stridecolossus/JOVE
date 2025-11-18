package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.*;
import java.util.stream.Stream;

import org.sarge.jove.common.Handle;

/**
 * A <i>block</i> is an area of device memory managed by a {@link MemoryPool} from which allocations can be served.
 * <p>
 * Note that the mapped region can be silently released by this implementation since only one region is permitted per memory instance.
 * <p>
 * @author Sarge
 */
class Block {
	private final DeviceMemory memory;
	private final List<BlockDeviceMemory> allocations = new ArrayList<>();
	private long next;
	private BlockDeviceMemory mapped;

	/**
	 * Constructor.
	 * @param memory Memory block
	 */
	Block(DeviceMemory memory) {
		this.memory = requireNonNull(memory);
	}

	/**
	 * @return Size of this block
	 */
	public long size() {
		return memory.size();
	}

	/**
	 * @return Free memory in this block
	 */
	long free() {
		final long total = allocations
				.stream()
				.filter(DeviceMemory.ALIVE)
				.mapToLong(DeviceMemory::size)
				.sum();

		return size() - total;
	}

	/**
	 * @return Remaining free memory in this block
	 */
	long remaining() {
		return size() - next;
	}

	/**
	 * @return Allocated memory in this block
	 */
	Stream<BlockDeviceMemory> allocations() {
		return allocations.stream();
	}

	/**
	 * Allocates memory from the <i>end</i> of this block.
	 * @param size Memory size
	 * @return New memory allocation
	 * @throws IllegalStateException if this block has been released
	 * @throws IllegalArgumentException if the given size is larger than the available free space
	 */
	BlockDeviceMemory allocate(long size) {
		// Validate
		requireOneOrMore(size);
		if(memory.isDestroyed()) {
			throw new IllegalStateException("Memory block has been released: " + this);
		}
		if(next + size > memory.size()) {
			throw new IllegalArgumentException(String.format("Allocation size exceeds free space: size=%d block=%s", size, this));
		}

		// Allocate from free space
		final var block = new BlockDeviceMemory(next, size);
		allocations.add(block);

		// Update free space pointer
		next += size;
		assert next <= memory.size();

		return block;
	}

	/**
	 * Destroys this block.
	 */
	void destroy() {
		memory.destroy();
		allocations.clear();
		mapped = null;
	}

	@Override
	public int hashCode() {
		return memory.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	/**
	 * Proxy implementation for memory allocated from this block.
	 */
	class BlockDeviceMemory implements DeviceMemory {
		private final long offset;
		private long size;
		private boolean destroyed;

		/**
		 * Constructor.
		 * @param offset		Offset into block
		 * @param size			Size
		 */
		private BlockDeviceMemory(long offset, long size) {
			this.offset = offset;
			this.size = size;
			assert offset + size <= memory.size();
		}

		@Override
		public Handle handle() {
			return memory.handle();
		}

		@Override
		public MemoryType type() {
			return memory.type();
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public Optional<Region> region() {
			if(mapped == this) {
				return memory.region();
			}
			else {
				return Optional.empty();
			}
		}

		@Override
		public Region map(long offset, long size) {
			checkAlive();
			memory.region().ifPresent(Region::unmap);
			mapped = this;
			return memory.map(offset, size);
		}

		/**
		 * Reallocates this device memory.
		 * @param size New size
		 * @return Reallocated memory
		 */
		BlockDeviceMemory reallocate(long size) {
			requireOneOrMore(size);
			if(memory.isDestroyed()) {
				throw new IllegalStateException("Block has been destroyed: " + this);
			}
			if(!destroyed) {
				throw new IllegalStateException("Block allocation canot be reallocated: " + this);
			}
			if(size > this.size) {
				throw new IllegalArgumentException("Reallocation size is larger than this memory: " + this);
			}

			// TODO - essentially orphans unused memory! => remove or resize this memory, and add new instance for remainder
			this.size = size;
			destroyed = false;

			return this;
		}

		@Override
		public boolean isDestroyed() {
			return destroyed || memory.isDestroyed();
		}

		@Override
		public void destroy() {
			checkAlive();
			if(mapped == this) {
				memory.region().get().unmap();
				mapped = null;
			}
			destroyed = true;
		}

		private void checkAlive() {
			if(isDestroyed()) {
				throw new IllegalStateException("Device memory has been destroyed: " + this);
			}
		}

		@Override
		public int hashCode() {
			return Objects.hash(memory, offset, size);
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
	}
}
