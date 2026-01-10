package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * An <i>index buffer</i> binds a drawing index to the pipeline.
 * @author Sarge
 */
public record IndexBuffer(VkIndexType type, VulkanBuffer buffer) {
	/**
	 * Required feature name for 8-bit indices.
	 */
	public static final String INDEX_TYPE_UINT8 = "indexTypeUint8";

	/**
	 * Constructor given a specific index data type.
	 * @param type			Index data type
	 * @param buffer		Underlying buffer
	 * @throws IllegalArgumentException if the given {@link #type} is invalid
	 * @throws IllegalStateException if the {@link #buffer} cannot be used as an {@link VkBufferUsageFlag#INDEX_BUFFER}
	 * @throws IllegalStateException if the index is larger than the {@code maxDrawIndexedIndexValue} hardware limit
	 * @throws UnsupportedOperationException if the index requires a device feature that is not enabled
	 */
	public IndexBuffer {
		requireNonNull(type);
		if(type == VkIndexType.NONE_NV) {
			throw new IllegalArgumentException("Invalid index type: " + type);
		}
		buffer.require(VkBufferUsageFlags.INDEX_BUFFER);
		validate(buffer, type);
	}

	/**
	 * Helper.
	 * Maps the given index data size to the equivalent Vulkan index type.
	 * @param size Index data size (bytes)
	 * @return Vulkan index type
	 * @throws IllegalArgumentException if {@link #size} is unsupported
	 */
	public static VkIndexType type(int size) {
		return switch(size) {
			case Byte.BYTES		-> VkIndexType.UINT8_EXT;
			case Short.BYTES	-> VkIndexType.UINT16;
			case Integer.BYTES	-> VkIndexType.UINT32;
			default -> throw new IllegalArgumentException("Unsupported index size: " + size);
		};
	}

	/**
	 * Creates a command to bind this index buffer.
	 * @return Command to bind this index buffer
	 * @throws IllegalStateException if the index is larger than the {@code maxDrawIndexedIndexValue} hardware limit
	 * @see #bind(long)
	 */
	public Command bind() {
		return bind(0L);
	}

	/**
	 * Creates a command to bind this index buffer.
	 * @param offset Buffer offset
	 * @return Command to bind this index buffer
	 */
	public Command bind(long offset) {
		buffer.checkOffset(offset);
		final VulkanBuffer.Library library = buffer.device().library();
		return commandBuffer -> library.vkCmdBindIndexBuffer(commandBuffer, buffer, offset, type);
	}

	/**
	 * Validates that the index is supported by the hardware.
	 */
	private static void validate(VulkanBuffer buffer, VkIndexType type) {
		final var device = buffer.device();
		switch(type) {
			case UINT8_EXT -> {
				device.features().require(INDEX_TYPE_UINT8);
			}

			case UINT16 -> {
				// A short index is always supported
			}

			case UINT32 -> {
				// Check buffer length is supported
				final int max = device.limits().get("maxDrawIndexedIndexValue");
				if(max >= 0) {
					final long size = buffer.length() / Integer.BYTES;
					if(size > max) {
						throw new IllegalStateException("Index too large: count=%d max=%d".formatted(size, max));
					}
				}
			}
		}
	}
}
