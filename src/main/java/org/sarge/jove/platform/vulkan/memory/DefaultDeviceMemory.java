package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Default implementation for memory allocated by the hardware.
 * @author Sarge
 */
public class DefaultDeviceMemory extends AbstractVulkanObject implements DeviceMemory {
	private final long size;

	private volatile DefaultRegion region;

	/**
	 * Constructor.
	 * @param handle		Memory pointer
	 * @param dev			Logical device context
	 * @param len			Size of this memory (bytes)
	 */
	public DefaultDeviceMemory(Pointer handle, DeviceContext dev, long len) {
		super(handle, dev);
		this.size = oneOrMore(len);
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public Optional<Region> region() {
		return Optional.ofNullable(region);
	}

	/**
	 * Mapped region implementation.
	 */
	@SuppressWarnings("hiding")
	private class DefaultRegion implements Region {
		private final Pointer ptr;
		private final long size;
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
			this.size = oneOrMore(size);
		}

		@Override
		public long size() {
			return size;
		}

		@Override
		public ByteBuffer buffer(long offset, long size) {
			checkMapped();
			if(offset + size > this.size) {
				throw new IllegalArgumentException(String.format("Buffer offset/length is larger than region: offset=%d size=%d region=%s", offset, size, this));
			}
			return ptr.getByteBuffer(offset, size);
		}

		@Override
		public synchronized void unmap() {
			// Validate mapping is active
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
					(this.size == that.size) &&
					this.ptr.equals(that.ptr);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("size", region)
					.append("offset", offset)
					.append("mem", DefaultDeviceMemory.this)
					.build();
		}
	}

	@Override
	public synchronized Region map(long offset, long size) {
		// Validate
		Check.zeroOrMore(offset);
		Check.oneOrMore(size);
		checkAlive();
		if(region != null) {
			throw new IllegalStateException("Device memory has already been mapped: " + this);
		}
		if(offset + size > this.size) {
			throw new IllegalArgumentException(String.format("Mapped region is larger than this device memory: offset=%d size=%d mem=%s", offset, size, this));
		}
		// TODO - check memory has VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT when map()

		// Map memory
		final DeviceContext dev = this.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = lib.factory().pointer();
		check(lib.vkMapMemory(dev, this, offset, size, 0, ref));

		// Create mapped region
		region = new DefaultRegion(ref.getValue(), offset, size);

		return region;
	}

	/**
	 * @throws IllegalStateException if this memory has been released
	 */
	private void checkAlive() {
		if(isDestroyed()) {
			throw new IllegalStateException("Device memory has been released: " + this);
		}
	}

	/**
	 * @throws IllegalStateException if this region has not been destroyed or invalidated
	 */
	private void checkMapped() {
		checkAlive();
		if(region == null) throw new IllegalStateException("Memory region has been invalidated: " + this);
	}

	@Override
	protected Destructor<DefaultDeviceMemory> destructor(VulkanLibrary lib) {
		return lib::vkFreeMemory;
	}

	@Override
	protected void release() {
		region = null;
	}

	@Override
	public int hashCode() {
		return Objects.hash(handle, size);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof DefaultDeviceMemory that) &&
				(this.size == that.size) &&
				this.handle.equals(that.handle);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handle", super.handle())
				.append("size", size)
				.append("mapped", region)
				.build();
	}
}
