package org.sarge.jove.scene;

/**
 * Stage in the rendering pipeline.
 * @author Sarge
 */
public interface RenderStage {
	/**
	 * Sorting order for rendering objects.
	 */
	enum SortOrder {
		/**
		 * Objects are un-sorted.
		 */
		NONE( null ),

		/**
		 * Sorted front-to-back, e.g. opaque objects leveraging the depth buffer.
		 */
		FRONT_TO_BACK( new NodeComparator( false ) ),

		/**
		 * Sorted back-to-front, e.g. transparent or translucent objects.
		 */
		BACK_TO_FRONT( new NodeComparator( true ) );

		private final NodeComparator comparator;

		private SortOrder( NodeComparator comparator ) {
			this.comparator = comparator;
		}

		/**
		 *
		 * @return Node comparator for this sort-order or <tt>null</tt> for {@link #NONE}
		 */
		public NodeComparator getComparator() {
			return comparator;
		}
	}

	/**
	 * @return Sort order objects rendered in this stage
	 */
	SortOrder getSortOrder();
}
