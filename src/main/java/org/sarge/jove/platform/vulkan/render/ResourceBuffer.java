package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireZeroOrMore;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;

/**
 * A <i>resource buffer</i> is an adapter for a buffer used as a descriptor resource, e.g. a {@link VkBufferUsageFlag#UNIFORM_BUFFER}.
 * @author Sarge
 */
public record ResourceBuffer(VkDescriptorType type, long offset, VulkanBuffer buffer) implements DescriptorSet.Resource {
	/**
	 * Constructor.
	 * @param buffer 		Underlying buffer
	 * @param type			Descriptor type
	 * @param offset		Buffer offset
	 * @throws IllegalArgumentException if the {@link #offset} exceeds the length of the {@link #buffer}
	 * @throws IllegalStateException if {@link #type} is not supported
	 * @throws IllegalStateException if this resource is too large for the hardware
	 */
	public ResourceBuffer {
		requireNonNull(type);
		requireZeroOrMore(offset);
		buffer.checkOffset(offset);
		validate(buffer, type);
	}

	private static void validate(VulkanBuffer buffer, VkDescriptorType type) {
		// Check buffer
		final VkBufferUsageFlags usage = switch(type) {
    		case UNIFORM_BUFFER, UNIFORM_BUFFER_DYNAMIC -> VkBufferUsageFlags.UNIFORM_BUFFER;
    		case STORAGE_BUFFER, STORAGE_BUFFER_DYNAMIC -> VkBufferUsageFlags.STORAGE_BUFFER;
    		// TODO - other buffers, e.g. texel
    		default -> throw new IllegalArgumentException("Unsupported descriptor type: " + type);
    	};
		buffer.require(usage);

		// Check length
		final var device = buffer.device();
		final int max = VulkanBuffer.maximum(usage, device.limits());
		if(buffer.length() > max) {
			throw new IllegalStateException("Buffer too large: limit=%d buffer=%s".formatted(max, buffer));
		}
	}

	@Override
	public VkDescriptorBufferInfo descriptor() {
		final var info = new VkDescriptorBufferInfo();
		info.buffer = buffer.handle();
		info.offset = offset;
		info.range = buffer.length();
		return info;
	}
}
