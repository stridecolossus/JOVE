package org.sarge.jove.material;

/**
 * Material parameter scope.
 * @author Sarge
 */
public enum PropertyScope {
	/**
	 * Updated per-frame, e.g. {@link RenderProperty#ELAPSED_TIME}
	 */
	FRAME,

	/**
	 * Updated per-node, e.g. {@link RenderProperty#MODELVIEW_MATRIX}
	 */
	NODE,

	/**
	 * Updated programatically (usually once).
	 */
	GLOBAL,
}
