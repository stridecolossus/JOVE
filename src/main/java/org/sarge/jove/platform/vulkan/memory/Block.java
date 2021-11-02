package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.lib.util.Check;

/**
 * A <i>block</i> is a chunk of memory managed by a {@link MemoryPool}.
 * @author Sarge
 */
class Block {
	/**
	 * Active memory filter.
	 */
	public static final Predicate<BlockDeviceMemory> ALIVE = Predicate.not(DeviceMemory::isDestroyed);

	private final DeviceMemory mem;
	private final List<BlockDeviceMemory> allocations = new ArrayList<>();
	private long next;

	/**
	 * Constructor.
	 * @param mem Memory block
	 */
	Block(DeviceMemory mem) {
		this.mem = notNull(mem);
	}

	/**
	 * @return Free memory in this block
	 */
	long free() {
		final long total = allocations.stream().filter(ALIVE).mapToLong(DeviceMemory::size).sum();
		return mem.size() - total;
	}

	/**
	 * @return Remaining free memory in this block
	 */
	long remaining() {
		return mem.size() - next;
	}

	/**
	 * @return Allocated memory in this block
	 */
	Stream<BlockDeviceMemory> allocations() {
		return allocations.stream();
	}

	/**
	 * Allocates memory from this end of this block.
	 * @param size Memory size
	 * @return New memory allocation
	 * @throws IllegalStateException if this block has been released
	 * @throws IllegalArgumentException if {@link #size} is larger than the available free space
	 */
	BlockDeviceMemory allocate(long size) {
		// Validate
		Check.oneOrMore(size);
		if(mem.isDestroyed()) throw new IllegalStateException("Memory block has been released: " + this);
		if(next + size > mem.size()) throw new IllegalArgumentException(String.format("Allocation size exceeds free space: size=%d block=%s", size, this));

		// Allocate from free space
		final BlockDeviceMemory alloc = new BlockDeviceMemory(next, size);
		allocations.add(alloc);

		// Update free space pointer
		next += size;
		assert next <= mem.size();

		return alloc;
	}

	/**
	 * Destroys this block.
	 */
	void destroy() {
		mem.close();
		allocations.clear();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("mem", mem)
				.append("free", next)
				.build();
	}

	/**
	 * Proxy implementation for memory allocated from this block.
	 */
	class BlockDeviceMemory implements DeviceMemory {
		private final long offset;
		private final long size;

		private boolean destroyed;

		/**
		 * Constructor.
		 * @param offset		Offset into block
		 * @param size			Size
		 */
		private BlockDeviceMemory(long offset, long size) {
			this.offset = offset;
			this.size = size;
			assert offset + size <= mem.size();
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
			checkAlive();
			mem.region().ifPresent(Region::unmap);
			return mem.map(offset, size);
		}

		/**
		 * Reallocates this memory.
		 */
		BlockDeviceMemory reallocate() {
			if(!destroyed) throw new IllegalStateException("Device memory has not been destroyed: " + this);
			if(mem.isDestroyed()) throw new IllegalStateException("Device memory cannot be reallocated: " + this);
			destroyed = false;
			return this;
		}

		@Override
		public boolean isDestroyed() {
			return destroyed || mem.isDestroyed();
		}

		@Override
		public synchronized void close() {
			checkAlive();
			destroyed = true;
		}

		private void checkAlive() {
			if(isDestroyed()) throw new IllegalStateException("Device memory has been destroyed: " + this);
		}

		@Override
		public int hashCode() {
			return Objects.hash(offset, size);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof BlockDeviceMemory that) &&
					(this.size == that.size) &&
					(this.offset == that.offset) &&
					(this.handle().equals(that.handle()));
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