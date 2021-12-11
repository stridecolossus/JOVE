package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.core.Command;

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
	public static class Builder {
		private boolean indexed;
		private int count;
		private int firstVertex;
		private int firstIndex;
		private int instanceCount = 1;
		private int firstInstance;

		/**
		 * Sets this as an <i>indexed</i> draw command.
		 */
		public Builder indexed() {
			indexed = true;
			return this;
		}

		/**
		 * Sets this as an indexed draw command.
		 * @param firstIndex First index
		 */
		public Builder firstIndex(int firstIndex) {
			this.firstIndex = zeroOrMore(firstIndex);
			return this;
		}

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
			if(indexed) {
				return (api, buffer) -> api.vkCmdDrawIndexed(buffer, count, instanceCount, firstIndex, firstVertex, firstInstance);
			}
			else {
				return (api, buffer) -> api.vkCmdDraw(buffer, count, instanceCount, firstVertex, firstInstance);
			}
		}
	}
}
