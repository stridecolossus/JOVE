package org.sarge.jove.scene;

/**
 * A <i>material</i> defines the visual characteristics for a set of renderable geometry.
 * <p>
 * Generally a material is used to {@link #bind()} a rendering pipeline.
 * <p>
 * @author Sarge
 */
public interface Material {
	/**
	 * @return Render queue for geometry using this material
	 */
	RenderQueue queue();

	/**
	 * Binds this material.
	 */
	void bind();
}
