package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.util.Objects;

import org.sarge.jove.model.Mesh;
import org.sarge.jove.platform.vulkan.core.*;

/**
 * A <i>draw command</i> is used to render a {@link Mesh}.
 * @author Sarge
 */
public class DrawCommand implements Command {
	private final int vertexCount;
	private final int instanceCount;
	private final int firstVertex;
	private final int firstInstance;
	private final Integer firstIndex;
	private final Library library;

	/**
	 * Constructor.
	 * @param vertexCount			Number of vertices
	 * @param instanceCount			Number of instances
	 * @param firstVertex			First vertex
	 * @param firstInstance			First instance
	 * @param firstIndex			Optional index
	 * @param library				Drawing library
	 */
	public DrawCommand(int vertexCount, int instanceCount, int firstVertex, int firstInstance, Integer firstIndex, Library library) {
		this.vertexCount = requireZeroOrMore(vertexCount);
		this.instanceCount = requireOneOrMore(instanceCount);
		this.firstVertex = requireZeroOrMore(firstVertex);
		this.firstInstance = requireZeroOrMore(firstInstance);
		this.firstIndex = firstIndex;
		this.library = requireNonNull(library);
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

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof DrawCommand that) &&
				(this.vertexCount == that.vertexCount) &&
				(this.instanceCount == that.instanceCount) &&
				(this.firstVertex == that.firstVertex) &&
				(this.firstInstance == that.firstInstance) &&
				Objects.equals(this.firstIndex, that.firstIndex);
	}

	@Override
	public String toString() {
		return String.format("DrawCommand[vertexCount=%d firstVertex=%d instanceCount=%d firstInstance=%d firstIndex=%s]", vertexCount, firstVertex, instanceCount, firstInstance, firstIndex);
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
		 * @param library Drawing library
		 * @return Draw command
		 */
		public DrawCommand build(Library library) {
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
