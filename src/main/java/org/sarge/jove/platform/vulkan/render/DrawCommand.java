package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.Validation.*;

import org.sarge.jove.model.Mesh;
import org.sarge.jove.platform.vulkan.VkBufferUsageFlag;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * A <i>draw command</i> is used to render a {@link Mesh}.
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
	 * Creates a simple draw command.
	 * @param count Number of vertices
	 * @return Simple draw command
	 */
	static DrawCommand draw(int count) {
		return new Builder().count(count).build();
	}

	/**
	 * Creates an indexed draw command.
	 * @param count Number of indices
	 * @return Indexed draw command
	 */
	static DrawCommand indexed(int count) {
		return new Builder().indexed().count(count).build();
	}

	/**
	 * Creates a draw command for the given mesh.
	 * @param mesh Mesh
	 * @return Draw command
	 */
	static DrawCommand of(Mesh mesh) {
		final int count = mesh.count();
		if(mesh.isIndexed()) {
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
		 * @see #indexed()
		 */
		public Builder count(int count) {
			this.count = requireZeroOrMore(count);
			return this;
		}

		/**
		 * Sets this as an <i>indexed</i> draw command starting at the <b>first</b> index.
		 * @see #indexed(int)
		 */
		public Builder indexed() {
			return indexed(0);
		}

		/**
		 * Sets this as an <i>indexed</i> draw command.
		 * @param firstIndex First index
		 */
		public Builder indexed(int firstIndex) {
			this.index = requireZeroOrMore(firstIndex);
			return this;
		}

		/**
		 * Sets the index of the first vertex (default is zero).
		 * @param firstVertex First vertex
		 */
		public Builder firstVertex(int firstVertex) {
			this.firstVertex = requireZeroOrMore(firstVertex);
			return this;
		}

		/**
		 * Sets the number of instances (default is one).
		 * @param instanceCount Number of instances
		 */
		public Builder instances(int instanceCount) {
			this.instanceCount = requireOneOrMore(instanceCount);
			return this;
		}

		/**
		 * Sets the index of the first instance (default is zero).
		 * @param firstInstance First instance
		 */
		public Builder firstInstance(int firstInstance) {
			this.firstInstance = requireZeroOrMore(firstInstance);
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
			this.offset = requireZeroOrMore(offset);
			return this;
		}

		/**
		 * Sets the draw count.
		 * @param count Draw count
		 */
		public IndirectBuilder count(int count) {
			this.count = requireZeroOrMore(count);
			return this;
		}

		/**
		 * Sets the vertex stride.
		 * @param stride Vertex stride
		 */
		public IndirectBuilder stride(int stride) {
			this.stride = requireZeroOrMore(stride);
			return this;
		}

		/**
		 * Constructs this indirect draw command.
		 * @param buffer Indirect buffer
		 * @return New indirect draw command
		 * @throws IllegalArgumentException if the buffer is not an {@link VkBufferUsageFlag#INDIRECT_BUFFER}
		 * @throws IllegalArgumentException if the offset is invalid for the given buffer
		 * @throws IllegalArgumentException if the draw count exceeds the hardware limit
		 */
		public DrawCommand build(VulkanBuffer buffer) {
			// Validate
			buffer.require(VkBufferUsageFlag.INDIRECT_BUFFER);
			buffer.checkOffset(offset);

			// Check indirect multi-draw is supported
			final DeviceContext dev = buffer.device();
// TODO
//			dev.features().require("multiDrawIndirect");
//
//			// Check the indirect draw count is supported by the hardware
//			final var limits = dev.limits();
//			final int max = limits.maxDrawIndirectCount;
//			if(count > max) throw new IllegalArgumentException("Invalid indirect draw count: count=%d max=%d".formatted(count, max));

			// Create command
			if(indexed) {
				return (lib, cmd) -> lib.vkCmdDrawIndexedIndirect(cmd, buffer, offset, count, stride);
			}
			else {
				return (lib, cmd) -> lib.vkCmdDrawIndirect(cmd, buffer, offset, count, stride);
			}
		}
	}
	// TODO - multiDrawIndirect

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
		void vkCmdDraw(CommandBuffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance);

		/**
		 * Draws indexed vertices.
		 * @param commandBuffer			Command buffer
		 * @param indexCount			Number of indices
		 * @param instanceCount			Number of instances
		 * @param firstIndex			First index
		 * @param firstVertex			First vertex index
		 * @param firstInstance			First instance
		 */
		void vkCmdDrawIndexed(CommandBuffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int firstVertex, int firstInstance);

		/**
		 * Indirect draw.
		 * @param commandBuffer			Command buffer
		 * @param buffer				Indirect buffer
		 * @param offset				Buffer offset
		 * @param drawCount				Draw count
		 * @param stride				Stride
		 */
		void vkCmdDrawIndirect(CommandBuffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride);

		/**
		 * Indirect indexed draw.
		 * @param commandBuffer			Command buffer
		 * @param buffer				Indirect buffer
		 * @param offset				Buffer offset
		 * @param drawCount				Draw count
		 * @param stride				Stride
		 */
		void vkCmdDrawIndexedIndirect(CommandBuffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride);
	}
}
