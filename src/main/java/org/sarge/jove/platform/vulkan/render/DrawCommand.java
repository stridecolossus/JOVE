package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import org.sarge.jove.model.Model;
import org.sarge.jove.platform.vulkan.common.Command;

/**
 * A <i>draw command</i> is used to render a model.
 * <p>
 * The {@link Builder} is used to specify the various parameters of the draw command.
 * <p>
 * The convenience {@link #draw(int)} and {@link #indexed(int)} factory methods are used to create simple draw commands (with all parameters defaulted).
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
		return new Builder().indexed(0).count(count).build();
	}

	/**
	 * Helper - Creates a draw command for the given model.
	 * @param model Model
	 * @return New draw command
	 */
	static DrawCommand of(Model model) {
		final int count = model.header().count();
		if(model.isIndexed()) {
			return indexed(count);
		}
		else {
			return draw(count);
		}
	}

	/**
	 * Builder for a draw command.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>By default the draw command created by this builder renders a number of vertices.</li>
	 * <li>The {@link #indexed(int)} method sets the command as <i>indexed</i>, i.e. a model with an index buffer.</li>
	 * <li>{@link #instanced(int, int)} sets the command to render multiple <i>instances</i>.</li>
	 * </ul>
	 * <p>
	 * Examples:
	 * <pre>
	 *  // Render a triangle
	 *  DrawCommand triangle = new Builder()
	 *  	.count(3)
	 *  	.build();
	 *
	 *  // Render an indexed model
	 *  DrawCommand indexed = new Builder()
	 *  	.indexed(0)
	 *  	.count(3)
	 *  	.firstVertex(1)
	 *  	.build();
	 *
	 *  // Render an instanced model
	 *  DrawCommand instanced = new Builder()
	 *  	.count(3)
	 *  	.instanced(2, 1)
	 *  	.build();
	 * </pre>
	 */
	public static class Builder {
		private boolean indexed;
		private int count;
		private int firstVertex;
		private int firstIndex;
		private int instanceCount = 1;
		private int firstInstance;

		/**
		 * Sets this as an indexed draw command.
		 * @param firstIndex First index
		 */
		public Builder indexed(int firstIndex) {
			indexed = true;
			this.firstIndex = zeroOrMore(firstIndex);
			return this;
		}

		/**
		 * Sets the number of vertices/indices.
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
		 * Sets this as an <i>instanced</i> draw command.
		 * @param instanceCount		Number of instances
		 * @param firstInstance		First instance
		 */
		public Builder instanced(int instanceCount, int firstInstance) {
			this.instanceCount = oneOrMore(instanceCount);
			this.firstInstance = zeroOrMore(firstInstance);
			return this;
		}

		/**
		 * Constructs this draw command.
		 * @return New draw command
		 */
		public DrawCommand build() {
			// TODO - verification
			if(indexed) {
				return (api, buffer) -> api.vkCmdDrawIndexed(buffer, count, instanceCount, firstIndex, firstVertex, firstInstance);
			}
			else {
				return (api, buffer) -> api.vkCmdDraw(buffer, count, instanceCount, firstVertex, firstInstance);
			}
		}
	}
}
