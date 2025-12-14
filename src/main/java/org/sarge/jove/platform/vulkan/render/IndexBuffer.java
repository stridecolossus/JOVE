package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.model.IndexedMesh;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * An <i>index buffer</i> binds a drawing index to the pipeline.
 * <p>
 * Note that the index can be represented as either {@code short} or {@code int} values depending on the length of the vertex data.
 * <p>
 * @see IndexedMesh.Index#isCompactIndex()
 * @author Sarge
 */
public record IndexBuffer(VkIndexType type, VulkanBuffer buffer) {
	/**
	 * Constructor given a specific index data type.
	 * @param type			Index type
	 * @param buffer		Underlying buffer
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

//	// TODO
// - determine element type: either literal (e.g. 32) or >= minimumElementBytes => policy?
// - make this (and VBO) transient with optional release (same as View)
// - fix doc above
//	public void write(Mesh.Index index) {
//		final int bytes = index.minimumElementBytes();
//	}

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
