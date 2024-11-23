package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;

/**
 * A <i>vertex buffer</i> binds vertex data to the pipeline.
 * @author Sarge
 */
public final class VertexBuffer extends VulkanBuffer {
	/**
	 * Constructor.
	 * @param buffer Underlying buffer
	 * @throws IllegalStateException if this buffer is not a {@link VkBufferUsageFlag#VERTEX_BUFFER}
	 */
	public VertexBuffer(VulkanBuffer buffer) {
		super(buffer);
		require(VkBufferUsageFlag.VERTEX_BUFFER);
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
	public static Command bind(int start, Collection<VertexBuffer> buffers) {
		return (lib, cmd) -> lib.vkCmdBindVertexBuffers(cmd, start, buffers.size(), buffers, new long[]{0});
	}
	// TODO - offsets
}
