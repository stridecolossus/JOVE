package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DescriptorResource;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.util.VulkanProperty;

/**
 * A <i>resource buffer</i> wraps a Vulkan buffer as a descriptor resource, e.g. a {@link VkBufferUsageFlag#UNIFORM_BUFFER}.
 * @author Sarge
 */
public class ResourceBuffer extends VulkanBuffer implements DescriptorResource {
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
	 * @see #usage(VkDescriptorType)
	 */
	public ResourceBuffer(VulkanBuffer buffer, VkDescriptorType type, long offset) {
		super(buffer);
		this.type = notNull(type);
		this.offset = zeroOrMore(offset);
		require(map(type));
		validate(offset);
		validate();
	}

	/**
	 * Maps the descriptor type to the corresponding buffer usage.
	 * @param type Descriptor type
	 * @return Required buffer usage flag
	 */
	public static VkBufferUsageFlag map(VkDescriptorType type) {
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
		// Determine max property key for this type of buffer
		final String key = switch(type) {
			case UNIFORM_BUFFER, UNIFORM_BUFFER_DYNAMIC -> "maxUniformBufferRange";
			case STORAGE_BUFFER, STORAGE_BUFFER_DYNAMIC -> "maxStorageBufferRange";
			default -> null;
		};

		// Ignore if none
		if(key == null) {
			return;
		}

		// Validate buffer size
		super
				.device()
				.provider()
				.property(new VulkanProperty.Key(key))
				.validate(length());
	}

	@Override
	public VkDescriptorType type() {
		return type;
	}

	@Override
	public void populate(VkWriteDescriptorSet write) {
		final var info = new VkDescriptorBufferInfo();
		info.buffer = handle();
		info.offset = offset;
		info.range = length();					// TODO - maxUniformBufferRange, needs to be a separate parameter otherwise offset MUST be zero!
		write.pBufferInfo = info;
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

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(type)
				.append("offset", offset)
				.build();
	}
}
