package org.sarge.jove.platform.vulkan.memory;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.*;

import java.lang.foreign.MemorySegment;
import java.util.Optional;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

/**
 * Default implementation that essentially wraps an FFM memory segment.
 * @author Sarge
 */
class DefaultDeviceMemory extends VulkanObject implements DeviceMemory {
	private final MemoryType type;
	private final long size;
	private MemorySegment region;

	/**
	 * Constructor.
	 * @param handle		Memory handle
	 * @param device		Logical device
	 * @param type			Type of memory
	 * @param size			Size of this memory (bytes)
	 */
	DefaultDeviceMemory(Handle handle, LogicalDevice device, MemoryType type, long size) {
		super(handle, device);
		this.type = requireNonNull(type);
		this.size = requireOneOrMore(size);
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
	public final Optional<MemorySegment> region() {
		return Optional.ofNullable(region);
	}

	@Override
	public MemorySegment map(long offset, long size) {
		// Validate
		requireZeroOrMore(offset);
		requireOneOrMore(size);
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
		final LogicalDevice device = this.device();
		final MemoryLibrary library = device.library();
		final var pointer = new Pointer(size);
		library.vkMapMemory(device, this, offset, size, 0, pointer);

		// Retrieve mapped region
		region = pointer.get();

		return region;
	}
	// TODO - region rounding if not host coherent (?)

	@Override
	public void unmap() {
		// Validate mapping is active
		checkAlive();
		checkMapped();

		// Release mapping
		final LogicalDevice device = device();
		final MemoryLibrary library = device.library();
		library.vkUnmapMemory(device, this);

		// Clear mapping
		region = null;
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
	 * @throws IllegalStateException if the region has been unmapped or invalidated
	 */
	private void checkMapped() {
		if(region == null) {
			throw new IllegalStateException("Memory is not mapped: " + this);
		}
	}

	@Override
	protected final Destructor<DefaultDeviceMemory> destructor() {
		final MemoryLibrary library = this.device().library();
		return library::vkFreeMemory;
	}

	@Override
	protected void release() {
		region = null;
	}

	@Override
	public int hashCode() {
		return this.handle().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof DefaultDeviceMemory that) &&
				(this.type == that.type) &&
				(this.size == that.size) &&
				this.handle().equals(that.handle());
	}
}
