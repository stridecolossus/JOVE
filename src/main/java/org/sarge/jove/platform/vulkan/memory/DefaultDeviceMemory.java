package org.sarge.jove.platform.vulkan.memory;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.ByteSource.Sink;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>default device memory</i> is the default implementation for memory allocated by the hardware.
 * @author Sarge
 */
public class DefaultDeviceMemory extends AbstractVulkanObject implements DeviceMemory {
	private final long size;

	private volatile Sink mapping;

	/**
	 * Constructor.
	 * @param handle		Memory pointer
	 * @param dev			Logical device context
	 * @param size			Size of this memory (bytes)
	 */
	public DefaultDeviceMemory(Pointer handle, DeviceContext dev, long size) {
		super(handle, dev);
		this.size = oneOrMore(size);
	}

	@Override
	public long size() {
		return size;
	}

	@Override
	public boolean isMapped() {
		return mapping != null;
	}

	// TODO - check memory has VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT when map()

	/**
	 * Mapped region implementation.
	 */
	private class MappedRegion implements Sink {
		private final Pointer ptr;
		private final long region;
		private final long offset;

		/**
		 * Constructor.
		 * @param ptr				Region memory pointer
		 * @param region			Size of this region
		 * @param offset			Offset
		 */
		private MappedRegion(Pointer ptr, long region, long offset) {
			this.ptr = notNull(ptr);
			this.region = oneOrMore(region);
			this.offset = zeroOrMore(offset);
		}

		@Override
		public void write(byte[] array) {
			validate(array.length);
			ptr.write(0, array, 0, array.length);
		}

		@Override
		public void write(ByteBuffer buffer) {
			final long len = buffer.remaining();
			final ByteBuffer bb = ptr.getByteBuffer(0, len);
			validate(len);
			bb.put(buffer);
		}

		/**
		 * @throws IllegalArgumentException if the given size is larger than this memory
		 */
		private void validate(long size) {
			checkMapped();
			if(size > this.region) {
				throw new IllegalArgumentException(String.format("Data is larger than this region: size=%d mem=%s", size, this));
			}
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof MappedRegion that) &&
					(this.region == that.region) &&
					(this.offset == that.offset) &&
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
	public synchronized Sink map(long size, long offset) {
		// Validate
		Check.oneOrMore(size);
		Check.zeroOrMore(offset);
		checkAlive();
		if(isMapped()) {
			throw new IllegalStateException("Device memory has already been mapped: " + this);
		}
		if(size + offset > this.size) {
			throw new IllegalArgumentException(String.format("Mapped region is larger than this device memory: size=%d offset=%d mem=%s", size, offset, this));
		}

		// Map memory
		final DeviceContext dev = this.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = lib.factory().pointer();
		check(lib.vkMapMemory(dev, this, offset, size, 0, ref));

		// Create mapped region
		mapping = new MappedRegion(ref.getValue(), size, offset);

		return mapping;
	}

	@Override
	public synchronized void unmap() {
		// Validate mapping is active
		checkMapped();

		// Release mapping
		final DeviceContext dev = device();
		final VulkanLibrary lib = dev.library();
		lib.vkUnmapMemory(dev, this);

		// Clear mapping
		mapping = null;
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
		if(mapping == null) throw new IllegalStateException("Memory region has been invalidated: " + this);
	}

	@Override
	protected Destructor<DefaultDeviceMemory> destructor(VulkanLibrary lib) {
		return lib::vkFreeMemory;
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
				.append("mapped", mapping)
				.build();
	}
}
