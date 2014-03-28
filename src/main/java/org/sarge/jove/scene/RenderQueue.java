package org.sarge.jove.scene;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Rendering queue.
 * @author Sarge
 */
public interface RenderQueue {
	/**
	 * Node sorting order.
	 */
	enum SortOrder {
		/**
		 * Unsorted, e.g. for sky-box.
		 */
		NONE,

		/**
		 * Nodes are sorted nearest first, e.g. for opaque objects.
		 */
		FRONT_TO_BACK,

		/**
		 * Nodes are sorted farthest first, e.g. for translucent objects.
		 */
		BACK_TO_FRONT;

		/**
		 * Applies this sort order to the given queue.
		 * @param nodes			Queue
		 * @param comparator	Distance comparator for the current scene
		 */
		public void sort( List<Node> nodes, Comparator<Node> comparator ) {
			// Ignore if not sorted
			if( this == NONE ) return;

			// Sort queue
			Collections.sort( nodes, comparator );

			// Reverse queue
			if( this == SortOrder.BACK_TO_FRONT ) {
				Collections.reverse( nodes );
			}
		}
	}

	/**
	 * Default render queues.
	 */
	enum Default implements RenderQueue {
		/**
		 * Opaque objects queue.
		 */
		OPAQUE( SortOrder.FRONT_TO_BACK ),

		/**
		 * Opaque objects queue without distance sorting.
		 */
		OPAQUE_UNSORTED( SortOrder.NONE ),

		/**
		 * Queue for objects rendered infinitely far away such as a sky-box.
		 */
		SKY( SortOrder.NONE ),

		/**
		 * Translucent queue with farthest objects rendered first to preserve blending.
		 */
		TRANSLUCENT( SortOrder.BACK_TO_FRONT ),

		/**
		 * Queue for objects that must be rendered last.
		 */
		POST( SortOrder.BACK_TO_FRONT ),

		/**
		 * Virtual queue for objects that are not rendered.
		 */
		NONE( null );

		private final SortOrder order;

		private Default( SortOrder order ) {
			this.order = order;
		}

		@Override
		public SortOrder getSortOrder() {
			return order;
		}
	}

	/**
	 * @return Object sorting order for this queue
	 */
	SortOrder getSortOrder();
}
