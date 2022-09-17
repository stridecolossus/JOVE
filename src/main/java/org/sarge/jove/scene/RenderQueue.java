package org.sarge.jove.scene;

/**
 * A <i>render queue</i> defines the render order for a set of geometry.
 * @author Sarge
 */
public record RenderQueue(int order, boolean reverse) implements Comparable<RenderQueue> {
	/**
	 * Default render queue for opaque geometry.
	 */
	public static final RenderQueue OPAQUE = new RenderQueue(0, false);

	/**
	 * Render queue for translucent objects that are rendered in reverse distance order after all other geometry.
	 */
	public static final RenderQueue TRANSLUCENT = new RenderQueue(Integer.MAX_VALUE, true);

	/**
	 * Constructor.
	 * @param order			Queue order
	 * @param reverse		Whether geometry should be reverse ordered by distance, i.e. for a translucent queue
	 */
	public RenderQueue {
		// Empty
	}

	@Override
	public int compareTo(RenderQueue that) {
		return this.order - that.order;
	}
}
