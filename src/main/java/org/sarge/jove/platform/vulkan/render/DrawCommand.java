package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;

/**
 * A <i>draw command</i> is used to render a model.
 * <p>
 * Draw commands are constructed using the {@link Builder} or the convenience factory methods.
 * <p>
 * Examples:
 * <pre>
 * // Draw a triangle
 * DrawCommand simple = DrawCommand.draw(3);
 *
 * // Draw an indexed triangle
 * DrawCommand indexed = DrawCommand.indexed(3);
 *
 * // Draw multiple instances
 * DrawCommand instanced = new DrawCommand.Builder()
 *     .indexed()
 *     .count(3)
 *     .instances(4)
 *     .build();
 * </pre>
 * <p>
 * @author Sarge
 */
public interface DrawCommand extends Command {
	/**
	 * Creates a draw command.
	 * @param count Number of vertices
	 * @return New draw command
	 */
	static DrawCommand draw(int count) {
		return new Builder().count(count).build();
	}

	/**
	 * Creates an indexed draw command.
	 * @param count Number of indices
	 * @return New indexed draw command
	 */
	static DrawCommand indexed(int count) {
		return new Builder().indexed().count(count).build();
	}

	/**
	 * Helper - Creates a draw command for the given model.
	 * @param model Model
	 * @return New draw command
	 */
	static DrawCommand of(Model model) {
		final int count = model.count();
		if(model.isIndexed()) {
			return indexed(count);
		}
		else {
			return draw(count);
		}
	}

	/**
	 * Builder for a draw command.
	 */
	class Builder {
		private int count;
		private Integer index;
		private int firstVertex;
		private int instanceCount = 1;
		private int firstInstance;

		/**
		 * Sets the number of vertices to draw.
		 * @param count Draw count
		 * @see #indexed(int)
		 */
		public Builder count(int count) {
			this.count = oneOrMore(count);
			return this;
		}

		/**
		 * Sets this as an <i>indexed</i> draw command starting at the <b>first</b> index.
		 */
		public Builder indexed() {
			return indexed(0);
		}

		/**
		 * Sets this as an <i>indexed</i> draw command.
		 * @param firstIndex First index
		 */
		public Builder indexed(int firstIndex) {
			this.index = zeroOrMore(firstIndex);
			return this;
		}

		/**
		 * Sets the index of the first vertex.
		 * @param firstVertex First vertex
		 */
		public Builder firstVertex(int firstVertex) {
			this.firstVertex = zeroOrMore(firstVertex);
			return this;
		}

		/**
		 * Sets the number of instances.
		 * @param instanceCount Number of instances
		 */
		public Builder instances(int instanceCount) {
			this.instanceCount = oneOrMore(instanceCount);
			return this;
		}

		/**
		 * Sets the index of the first instance.
		 * @param firstInstance First instance
		 */
		public Builder firstInstance(int firstInstance) {
			this.firstInstance = zeroOrMore(firstInstance);
			return this;
		}

		/**
		 * Constructs this draw command.
		 * @return New draw command
		 */
		public DrawCommand build() {
			if(index == null) {
				return (api, buffer) -> api.vkCmdDraw(buffer, count, instanceCount, firstVertex, firstInstance);
			}
			else {
				return (api, buffer) -> api.vkCmdDrawIndexed(buffer, count, instanceCount, index, firstVertex, firstInstance);
			}
		}
		// TODO - maxDrawIndexedIndexValue - UINT32
		// TODO - restart mask, not UINT32???
	}

	/**
	 * Builder for an <i>indirect</i> draw command.
	 */
	class IndirectBuilder {
		private boolean indexed;
		private long offset;
		private int count = 1;
		private int stride;

		/**
		 * Sets this as an indexed draw command.
		 */
		public IndirectBuilder indexed() {
			indexed = true;
			return this;
		}

		/**
		 * Sets the buffer offset.
		 * @param offset Buffer offset
		 */
		public IndirectBuilder offset(long offset) {
			this.offset = zeroOrMore(offset);
			return this;
		}

		/**
		 * Sets the draw count.
		 * @param count Draw count
		 */
		public IndirectBuilder count(int count) {
			this.count = oneOrMore(count);
			return this;
		}

		/**
		 * Sets the vertex stride.
		 * @param stride Vertex stride
		 */
		public IndirectBuilder stride(int stride) {
			this.stride = zeroOrMore(stride);
			return this;
		}

		/**
		 * Constructs this indirect draw command.
		 * @param buffer Indirect buffer
		 * @return New indirect draw command
		 * @throws IllegalArgumentException if the {@code offset} is invalid for the given buffer
		 * @throws IllegalArgumentException if the buffer is not an {@link VkBufferUsageFlag#INDIRECT_BUFFER}
		 */
		public DrawCommand build(VulkanBuffer buffer) {
			// Validate
			buffer.require(VkBufferUsageFlag.INDIRECT_BUFFER);
			buffer.validate(offset);

			// Create command
			if(indexed) {
				return (api, cmd) -> api.vkCmdDrawIndexedIndirect(cmd, buffer, offset, count, stride);
			}
			else {
				return (api, cmd) -> api.vkCmdDrawIndirect(cmd, buffer, offset, count, stride);
			}
		}
		// TODO - maxDrawIndirectCount (1)
		// TODO - multiDrawIndirect => count > 1
	}

	/**
	 * Drawing API.
	 */
	interface Library {
		/**
		 * Draws vertices.
		 * @param commandBuffer			Command buffer
		 * @param vertexCount			Number of vertices
		 * @param instanceCount			Number of instances
		 * @param firstVertex			First vertex index
		 * @param firstInstance			First index index
		 */
		void vkCmdDraw(Command.Buffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance);

		/**
		 * Draws indexed vertices.
		 * @param commandBuffer			Command buffer
		 * @param indexCount			Number of indices
		 * @param instanceCount			Number of instances
		 * @param firstIndex			First index
		 * @param firstVertex			First vertex index
		 * @param firstInstance			First instance
		 */
		void vkCmdDrawIndexed(Command.Buffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int firstVertex, int firstInstance);

		/**
		 * Indirect draw.
		 * @param commandBuffer			Command buffer
		 * @param buffer				Indirect buffer
		 * @param offset				Buffer offset
		 * @param drawCount				Draw count
		 * @param stride				Stride
		 */
		void vkCmdDrawIndirect(Command.Buffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride);

		/**
		 * Indirect indexed draw.
		 * @param commandBuffer			Command buffer
		 * @param buffer				Indirect buffer
		 * @param offset				Buffer offset
		 * @param drawCount				Draw count
		 * @param stride				Stride
		 */
		void vkCmdDrawIndexedIndirect(Command.Buffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride);
	}
}
