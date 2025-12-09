package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.*;

import org.sarge.jove.model.*;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * A <i>draw command</i> is used to render a {@link Mesh}.
 * @author Sarge
 */
public record DrawCommand(int vertexCount, int instanceCount, int firstVertex, int firstInstance, Integer firstIndex, Library library) implements Command {
	/**
	 * Constructor.
	 * @param vertexCount			Number of vertices
	 * @param instanceCount			Number of instances
	 * @param firstVertex			First vertex
	 * @param firstInstance			First instance
	 * @param firstIndex			Optional starting index
	 * @param library				Drawing library
	 */
	public DrawCommand {
		requireZeroOrMore(vertexCount);
		requireOneOrMore(instanceCount);
		requireZeroOrMore(firstVertex);
		requireZeroOrMore(firstInstance);
		requireNonNull(library);
	}

	@Override
	public void execute(Buffer buffer) {
		if(firstIndex == null) {
			library.vkCmdDraw(buffer, vertexCount, instanceCount, firstVertex, firstInstance);
		}
		else {
			library.vkCmdDrawIndexed(buffer, vertexCount, instanceCount, firstIndex, firstVertex, firstInstance);
		}
	}

	/**
	 * Creates a simple draw command for the given number of vertices.
	 * @param vertexCount Number of vertices
	 * @param device Logical device
	 * @return Simple draw command
	 */
	public static DrawCommand of(int vertexCount, LogicalDevice device) {
		return new Builder().vertexCount(vertexCount).build(device);
	}

	/**
	 * Helper.
	 * Creates a draw command for the given mesh.
	 * @param mesh		Mesh
	 * @param device	Logical device
	 * @return Mesh draw command
	 */
	public static DrawCommand of(Mesh mesh, LogicalDevice device) {
		final int count = mesh.count();
		if(mesh instanceof IndexedMesh) {
			return of(count, device);
		}
		else {
			return new Builder()
					.vertexCount(count)
					.indexed()
					.build(device);
		}
	}

	/**
	 * Builder for a draw command.
	 */
	public static class Builder {
		private int vertexCount;
		private int firstVertex;
		private int instanceCount = 1;
		private int firstInstance;
		private Integer firstIndex;

		/**
		 * Sets the number of vertices to draw.
		 * @param count Draw count
		 * @see #indexed()
		 */
		public Builder vertexCount(int vertexCount) {
			this.vertexCount = vertexCount;
			return this;
		}

		/**
		 * Sets the number of instances (default is one).
		 * @param instanceCount Number of instances
		 */
		public Builder instanceCount(int instanceCount) {
			this.instanceCount = instanceCount;
			return this;
		}

		/**
		 * Sets the index of the first vertex (default is zero).
		 * @param firstVertex First vertex
		 */
		public Builder firstVertex(int firstVertex) {
			this.firstVertex = firstVertex;
			return this;
		}

		/**
		 * Sets the index of the first instance (default is zero).
		 * @param firstInstance First instance
		 */
		public Builder firstInstance(int firstInstance) {
			this.firstInstance = firstInstance;
			return this;
		}

		/**
		 * Sets this as an <i>indexed</i> draw command starting at the <b>first</b> index.
		 * @see #firstIndex(int)
		 */
		public Builder indexed() {
			return firstIndex(0);
		}

		/**
		 * Sets the first index.
		 * @param firstIndex First index
		 */
		public Builder firstIndex(int firstIndex) {
			this.firstIndex = firstIndex;
			return this;
		}

		/**
		 * Constructs this draw command.
		 * @param device Logical device
		 * @return Draw command
		 */
		public DrawCommand build(LogicalDevice device) {
			final Library library = device.library();
			return new DrawCommand(vertexCount, instanceCount, firstVertex, firstInstance, firstIndex, library);
		}
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
		void vkCmdDraw(Buffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance);

		/**
		 * Draws indexed vertices.
		 * @param commandBuffer			Command buffer
		 * @param indexCount			Number of indices
		 * @param instanceCount			Number of instances
		 * @param firstIndex			First index
		 * @param firstVertex			First vertex index
		 * @param firstInstance			First instance
		 */
		void vkCmdDrawIndexed(Buffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int firstVertex, int firstInstance);

		/**
		 * Indirect draw.
		 * @param commandBuffer			Command buffer
		 * @param buffer				Indirect buffer
		 * @param offset				Buffer offset
		 * @param drawCount				Draw count
		 * @param stride				Stride
		 */
		void vkCmdDrawIndirect(Buffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride);

		/**
		 * Indirect indexed draw.
		 * @param commandBuffer			Command buffer
		 * @param buffer				Indirect buffer
		 * @param offset				Buffer offset
		 * @param drawCount				Draw count
		 * @param stride				Stride
		 */
		void vkCmdDrawIndexedIndirect(Buffer commandBuffer, VulkanBuffer buffer, long offset, int drawCount, int stride);
	}
}
