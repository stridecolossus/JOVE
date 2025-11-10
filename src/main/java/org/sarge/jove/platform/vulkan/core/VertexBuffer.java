package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer.Library;

/**
 * A <i>vertex buffer</i> binds vertex data to the pipeline.
 * @author Sarge
 */
public record VertexBuffer(VulkanBuffer buffer) {
	/**
	 * Constructor.
	 * @param buffer Underlying buffer
	 * @throws IllegalStateException if the given buffer is not a {@link VkBufferUsageFlag#VERTEX_BUFFER}
	 */
	public VertexBuffer {
		buffer.require(VkBufferUsageFlag.VERTEX_BUFFER);
	}

	/**
	 * Creates a command to bind this vertex buffer to a pipeline.
	 * @param binding Binding index
	 * @return Bind command
	 * @see #bind(int, Collection)
	 */
	public Command bind(int binding) {
		return bind(binding, List.of(this));
	}

	/**
	 * Creates a command to bind a collection of vertex buffers.
	 * @param start 		Start binding index
	 * @param buffers		Vertex buffers to bind
	 * @return Bind command
	 */
	public static Command bind(int start, List<VertexBuffer> buffers) {
		final Library library = buffers
				.getFirst()
				.buffer()
				.device()
				.library();

		final var array = buffers
				.stream()
				.map(VertexBuffer::buffer)
				.toArray(VulkanBuffer[]::new);

		return buffer -> library.vkCmdBindVertexBuffers(buffer, start, array.length, array, new long[]{0});
	}
}
