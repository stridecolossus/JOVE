package org.sarge.jove.platform.vulkan.core;

import java.util.*;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;

import com.sun.jna.Pointer;

/**
 * A <i>vertex buffer</i> binds vertex data to the pipeline.
 * @author Sarge
 */
public class VertexBuffer extends VulkanBuffer {
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
	 * Creates a command to bind this buffer as a vertex buffer (VBO).
	 * @param binding Binding index
	 * @return Command to bind this buffer
	 * @see #bind(int, Collection)
	 */
	public Command bind(int binding) {
		return bind(binding, List.of(this));
	}

	/**
	 * Creates a command to bind a collection of vertex buffers.
	 * @param start 		Start binding index
	 * @param buffers		Buffers to bind
	 * @return Command to bind the given buffers
	 */
	public static Command bind(int start, Collection<VertexBuffer> buffers) {
		final Pointer array = NativeObject.array(buffers);
		return (api, cmd) -> api.vkCmdBindVertexBuffers(cmd, start, buffers.size(), array, new long[]{0});
	}
	// TODO - offsets
}
