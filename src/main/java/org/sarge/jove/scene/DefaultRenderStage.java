package org.sarge.jove.scene;

/**
 * Built-in render stage definitions.
 * @author Sarge
 */
public enum DefaultRenderStage implements RenderStage {
	/**
	 * Render stage for opaque objects drawn nearest first (to take advantage of depth buffer testing).
	 */
	OPAQUE( SortOrder.FRONT_TO_BACK ),

	/**
	 * Stage for sky objects drawn infinitely far away.
	 */
	SKY( SortOrder.NONE ),

	/**
	 * Stage for translucent objects that must be rendered back-to-front.
	 */
	TRANSLUCENT( SortOrder.BACK_TO_FRONT ),

	/**
	 * Stage for objects that must be rendered last, e.g. particle effects, shadows, etc.
	 */
	POST_PROCESS( SortOrder.BACK_TO_FRONT );

	private final SortOrder order;

	private DefaultRenderStage( SortOrder order ) {
		this.order = order;
	}

	@Override
	public SortOrder getSortOrder() {
		return order;
	}
}
