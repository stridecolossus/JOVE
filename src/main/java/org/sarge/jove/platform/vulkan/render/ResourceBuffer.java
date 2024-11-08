package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireZeroOrMore;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;

/**
 * A <i>resource buffer</i> wraps a Vulkan buffer as a descriptor resource, e.g. a {@link VkBufferUsageFlag#UNIFORM_BUFFER}.
 * @author Sarge
 */
public final class ResourceBuffer extends VulkanBuffer implements DescriptorResource {
	private final VkDescriptorType type;
	private final long offset;

	/**
	 * Constructor.
	 * @param buffer 		Underlying buffer
	 * @param type			Descriptor type
	 * @param offset		Buffer offset
	 * @throws IllegalStateException if {@link #type} is not supported by this buffer
	 * @throws IllegalStateException if this resource is too large for the hardware
	 * @throws IllegalArgumentException if the {@link #offset} exceeds the {@link #length()} of this buffer
	 */
	public ResourceBuffer(VulkanBuffer buffer, VkDescriptorType type, long offset) {
		super(buffer);
		this.type = requireNonNull(type);
		this.offset = requireZeroOrMore(offset);
		require(map(type));
		checkOffset(offset);
		validate();
	}

	/**
	 * Maps the descriptor type to the corresponding buffer usage.
	 * @param type Descriptor type
	 * @return Required buffer usage flag
	 */
	private static VkBufferUsageFlag map(VkDescriptorType type) {
		return switch(type) {
			case UNIFORM_BUFFER, UNIFORM_BUFFER_DYNAMIC -> VkBufferUsageFlag.UNIFORM_BUFFER;
			case STORAGE_BUFFER, STORAGE_BUFFER_DYNAMIC -> VkBufferUsageFlag.STORAGE_BUFFER;
			// TODO - other buffers, e.g. texel
			default -> throw new IllegalArgumentException("Invalid descriptor type: " + type);
		};
	}

	/**
	 * @throws IllegalStateException if this resource is too large for the hardware
	 */
	private void validate() {
		// Determine max buffer length
		final var limits = this.device().limits();
		final int max = switch(type) {
			case UNIFORM_BUFFER, UNIFORM_BUFFER_DYNAMIC -> limits.maxUniformBufferRange;
			case STORAGE_BUFFER, STORAGE_BUFFER_DYNAMIC -> limits.maxStorageBufferRange;
			default -> 0;
		};

		// Ignore if none
		if(max == 0) {
			return;
		}

		// Validate buffer size
		final long len = this.length();
		if(len > max) {
			throw new IllegalStateException("Buffer too large: length=%d limit=%d".formatted(len, max));
		}
	}

	@Override
	public VkDescriptorType type() {
		return type;
	}

	@Override
	public VkDescriptorBufferInfo build() {
		final var info = new VkDescriptorBufferInfo();
		info.buffer = handle();
		info.offset = offset;
		info.range = length();					// TODO - maxUniformBufferRange, needs to be a separate parameter otherwise offset MUST be zero!
		return info;
	}

	/**
	 * Offsets this resource buffer.
	 * @param offset Buffer offset
	 * @return Offset buffer
	 */
	public ResourceBuffer offset(long offset) {
		return new ResourceBuffer(this, type, this.offset + offset);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof ResourceBuffer that) &&
				(this.type == that.type()) &&
				(this.offset == that.offset) &&
				super.equals(obj);
	}
}
