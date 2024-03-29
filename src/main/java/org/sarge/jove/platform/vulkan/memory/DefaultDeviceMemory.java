package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.nio.ByteBuffer;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Default implementation.
 * @author Sarge
 */
class DefaultDeviceMemory extends VulkanObject implements DeviceMemory {
	private final MemoryType type;
	private final long size;
	private Region region;

	/**
	 * Constructor.
	 * @param handle		Memory pointer
	 * @param dev			Logical device
	 * @param type			Type of memory
	 * @param size			Size of this memory (bytes)
	 */
	DefaultDeviceMemory(Handle handle, DeviceContext dev, MemoryType type, long size) {
		super(handle, dev);
		this.type = notNull(type);
		this.size = oneOrMore(size);
	}

	@Override
	public final MemoryType type() {
		return type;
	}

	@Override
	public final long size() {
		return size;
	}

	@Override
	public final Optional<Region> region() {
		return Optional.ofNullable(region);
	}

	/**
	 * Mapped region implementation.
	 */
	private class DefaultRegion implements Region {
		private final Pointer ptr;
		private final long segment;
		private final long offset;

		/**
		 * Constructor.
		 * @param ptr				Region memory pointer
		 * @param offset			Offset
		 * @param size				Size of this region
		 */
		private DefaultRegion(Pointer ptr, long offset, long size) {
			this.ptr = notNull(ptr);
			this.offset = zeroOrMore(offset);
			this.segment = oneOrMore(size);
		}

		@Override
		public long size() {
			return segment;
		}

		@Override
		public ByteBuffer buffer(long offset, long size) {
			checkAlive();
			checkMapped();
			if(offset + size > segment) {
				throw new IllegalArgumentException("Buffer offset/length larger than region: offset=%d size=%d region=%s".formatted(offset, size, this));
			}
			return ptr.getByteBuffer(offset, size);
		}

		@Override
		public void unmap() {
			// Validate mapping is active
			checkAlive();
			checkMapped();

			// Release mapping
			final DeviceContext dev = device();
			final VulkanLibrary lib = dev.library();
			lib.vkUnmapMemory(dev, DefaultDeviceMemory.this);

			// Clear mapping
			region = null;
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof DefaultRegion that) &&
					(this.offset == that.offset) &&
					(this.segment == that.segment) &&
					this.ptr.equals(that.ptr);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append("offset", offset)
					.append("size", segment)
					.build();
		}
	}

	@Override
	public Region map(long offset, long size) {
		// Validate
		Check.zeroOrMore(offset);
		Check.oneOrMore(size);
		checkAlive();
		if(region != null) {
			throw new IllegalStateException("Device memory has already been mapped: " + this);
		}
		if(offset + size > this.size) {
			throw new IllegalArgumentException("Mapped region is larger than this device memory: offset=%d size=%d mem=%s".formatted(offset, size, this));
		}
		if(!type.isHostVisible()) {
			throw new IllegalStateException("Device memory is not host visible: " + this);
		}

		// Map memory
		final DeviceContext dev = this.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		check(lib.vkMapMemory(dev, this, offset, size, 0, ref));

		// Create mapped region
		region = new DefaultRegion(ref.getValue(), offset, size);

		return region;
	}
	// TODO - region rounding if not host coherent

	/**
	 * @throws IllegalStateException if this memory has been released
	 */
	private void checkAlive() {
		if(isDestroyed()) {
			throw new IllegalStateException("Device memory has been released: " + this);
		}
	}

	/**
	 * @throws IllegalStateException if the region has been unmapped or invalidated
	 */
	private void checkMapped() {
		if(region == null) {
			throw new IllegalStateException("Memory region has been invalidated: " + this);
		}
	}

	@Override
	protected final Destructor<DefaultDeviceMemory> destructor(VulkanLibrary lib) {
		return lib::vkFreeMemory;
	}

	@Override
	protected void release() {
		// TODO - do we need to explicitly unmap?
		region = null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(handle, type, size);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof DefaultDeviceMemory that) &&
				(this.type == that.type) &&
				(this.size == that.size) &&
				this.handle.equals(that.handle);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("type", type)
				.append("size", size)
				.append("mapped", region)
				.build();
	}
}
