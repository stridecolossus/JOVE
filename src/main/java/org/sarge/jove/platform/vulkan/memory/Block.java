package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.lib.util.Check;

/**
 * A <i>block</i> is an area of device memory managed by a {@link MemoryPool} from which allocations can be served.
 * <p>
 * Note that the mapped region can be silently released by this implementation since only one region is permitted per memory instance.
 * <p>
 * @author Sarge
 */
class Block {
	/**
	 * Active memory filter.
	 */
	public static final Predicate<DeviceMemory> ALIVE = Predicate.not(DeviceMemory::isDestroyed);

	private final DeviceMemory mem;
	private final List<BlockDeviceMemory> allocations = new ArrayList<>();
	private long next;
	private BlockDeviceMemory mapped;

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
	Stream<? extends DeviceMemory> allocations() {
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
		mem.destroy();
		allocations.clear();
		mapped = null;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("mem", mem)
				.append("next", next)
				.append("allocations", allocations.size())
				.append("mapped", mapped)
				.build();
	}

	/**
	 * Proxy implementation for memory allocated from this block.
	 */
	private class BlockDeviceMemory implements DeviceMemory {
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
			if(mapped == this) {
				return mem.region();
			}
			else {
				return Optional.empty();
			}
		}

		@Override
		public Region map(long offset, long size) {
			checkAlive();
			mem.region().ifPresent(Region::unmap);
			mapped = this;
			return mem.map(offset, size);
		}

		@Override
		public DeviceMemory reallocate() {
			if(!destroyed) throw new IllegalStateException("Block allocation cannot be reallocated: " + this);
			if(mem.isDestroyed()) throw new IllegalStateException("Block has been destroyed: " + this);
			destroyed = false;
			return this;
		}

		@Override
		public boolean isDestroyed() {
			return destroyed || mem.isDestroyed();
		}

		@Override
		public void destroy() {
			checkAlive();
			destroyed = true;
		}

		private void checkAlive() {
			if(isDestroyed()) throw new IllegalStateException("Device memory has been destroyed: " + this);
		}

		@Override
		public int hashCode() {
			return Objects.hash(mem, offset, size);
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
					.append("mapped", mapped == this)
					.append("mem", mem)
					.build();
		}
	}
}
