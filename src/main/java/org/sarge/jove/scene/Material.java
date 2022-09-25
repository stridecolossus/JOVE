package org.sarge.jove.scene;

/**
 * A <i>material</i> defines the visual characteristics for a set of renderable geometry.
 * @author Sarge
 */
public interface Material extends Renderable {
	/**
	 * @return Render queue for geometry using this material
	 */
	RenderQueue queue();
}
