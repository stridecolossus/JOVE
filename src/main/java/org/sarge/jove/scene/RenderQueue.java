package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;

/**
 * A <i>render queue</i> is an ordered list of nodes to be rendered.
 * @author Sarge
 */
public final class RenderQueue {
	/**
	 * Sort order for this queue.
	 */
	public enum Order {
		/**
		 * Queue is unordered.
		 */
		NONE,

		/**
		 * Objects are sorted nearest first (front-to-back).
		 */
		NEAREST,

		/**
		 * Objects are sorted farthest first (back-to-front).
		 */
		FARTHEST
	}

	/**
	 * Renderable model.
	 */
	public interface Renderable {
		/**
		 * Renders this model.
		 */
		void render();
	}

	/**
	 * A render queue <i>entry</i> defines a renderable model for a given node.
	 * @author Sarge
	 */
	public static final class Entry {
		/**
		 * Empty model entry.
		 */
		public static final Entry NONE = new Entry();

		private final Renderable model;
		private final RenderQueue queue;

		/**
		 * Constructor.
		 * @param model
		 * @param queue
		 */
		public Entry(Renderable model, RenderQueue queue) {
			this.model = notNull(model);
			this.queue = notNull(queue);
		}

		/**
		 * Empty model constructor.
		 */
		private Entry() {
			this.model = null;
			this.queue = null;
		}

		/**
		 * @return Renderable model
		 */
		public Renderable model() {
			return model;
		}

		/**
		 * @return Render queue for this model
		 */
		public RenderQueue queue() {
			return queue;
		}

		@Override
		public boolean equals(Object that) {
			return EqualsBuilder.reflectionEquals(this, that);
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	/**
	 * Enumerates the render queues used in the given scene-graph.
	 * This is a convenience method, in general the client should maintain the collection of active render queues.
	 * @param node Root node
	 * @return Render queues
	 */
	public static Collection<RenderQueue> queues(Node root) {
		final Collection<RenderQueue> queues = new HashSet<>();
		final Node.Visitor visitor = node -> {
			final var entry = node.model();
			if(entry != RenderQueue.Entry.NONE) {
				queues.add(entry.queue);
			}
			return true;
		};
		root.accept(visitor);
		return queues;
	}

	private final Order order;
	private final List<Node> queue = new ArrayList<>();

	/**
	 * Constructor.
	 * @param order Object sort order
	 */
	public RenderQueue(Order order) {
		this.order = notNull(order);
	}

	/**
	 * @return Queue sort order
	 */
	public Order order() {
		return order;
	}

	/**
	 * Orders this render queue relative to the given camera position.
	 * @param pos Camera position
	 * @return Ordered queue
	 */
	public Stream<Node> queue(Point pos) {
		// Enumerate visible nodes
		final var stream = queue.stream().filter(Node::isVisible);

		// Apply order
		if(order == Order.NONE) {
			return stream;
		}
		else {
			final Comparator<Node> comparator = comparator(pos);
			return stream.parallel().sorted(comparator);
		}
	}

	/**
	 * Creates a distance ordering comparator.
	 * @param pos Camera position
	 * @return Comparator
	 */
	private Comparator<Node> comparator(Point pos) {
		// Create distance comparator
		final Comparator<Node> comparator = (a, b) -> {
			if(distance(pos, a) < distance(pos, b)) {
				return -1;
			}
			else {
				return +1;
			}
		};

		// Adapt by queue order
		switch(order) {
		case NEAREST:		return comparator;
		case FARTHEST:		return comparator.reversed();
		default:			throw new RuntimeException();
		}
	}

	/**
	 * Calculates the distance-squared between the camera position and a given node.
	 * @param pos		Camera position
	 * @param node		Node
	 * @return Distance
	 */
	private static float distance(Point pos, Node node) {
		final Matrix matrix = node.transform().matrix();
		final Point pt = new Point(matrix.get(0, 3), matrix.get(1, 3), matrix.get(2, 3));
		return pos.distance(pt);
	}

	/**
	 * Adds a node to this queue.
	 * @param node Node
	 */
	void add(Node node) {
		assert !queue.contains(node);
		queue.add(node);
	}

	/**
	 * Removes a node from this queue.
	 * @param node Node
	 */
	void remove(Node node) {
		assert queue.contains(node);
		queue.remove(node);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
			.append("order", order)
			.append("size", queue.size())
			.toString();
	}
}
