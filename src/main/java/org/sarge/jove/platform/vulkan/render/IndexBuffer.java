package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.model.IndexedMesh;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * An <i>index buffer</i> binds a drawing index to the pipeline.
 * <p>
 * Note that the index is represented as either {@code short} or {@code int} values
 * depending on the length of the vertex data referred to, specified by {@link VkIndexType}.
 * <p>
 * @see IndexedMesh#isCompactIndex()
 * @author Sarge
 */
public record IndexBuffer(VkIndexType type, VulkanBuffer buffer) {
	/**
	 * Constructor given a specific index data type.
	 * @param type			Index type
	 * @param buffer		Index buffer
	 * @throws IllegalArgumentException if the given {@link #type} is invalid
	 * @throws IllegalStateException if the {@link #buffer} cannot be used as an {@link VkBufferUsageFlag#INDEX_BUFFER}
	 */
	public IndexBuffer {
		requireNonNull(type);
		if(type == VkIndexType.NONE_NV) {
			throw new IllegalArgumentException("Invalid index type: " + type);
		}
		buffer.require(VkBufferUsageFlags.INDEX_BUFFER);
	}

	/**
	 * @return Index type
	 */
	public VkIndexType type() {
		return type;
	}

	/**
	 * Creates a command to bind this buffer.
	 * @param offset Buffer offset
	 * @return Command to bind this index buffer
	 * @throws IllegalStateException if the index is larger than the {@code maxDrawIndexedIndexValue} hardware limit
	 */
	public Command bind(long offset) {
		buffer.checkOffset(offset);
		validateLimit();
		final VulkanBuffer.Library library = buffer.device().library();
		return commandBuffer -> library.vkCmdBindIndexBuffer(commandBuffer, buffer, offset, type);
	}

	/**
	 * @throws IllegalStateException if the index is larger than the hardware limit
	 */
	private void validateLimit() {
		// A short index is always supported
		if(type == VkIndexType.UINT16) {
			return;
		}

		// Ignore if unlimited
		final int max = buffer.device().limits().get("maxDrawIndexedIndexValue");
		if(max == -1) {
			return;
		}

		// Otherwise check buffer length is supported
		final long count = buffer.length() / Integer.BYTES;
		if(count > max) {
			throw new IllegalStateException("Index too large: count=%d max=%d index=%s".formatted(count, max, this));
		}
	}
}
