package org.sarge.jove.scene.graph;

/**
 * A <i>material</i> defines the visual characteristics for a set of renderable geometry.
 * @author Sarge
 */
public interface Material extends Renderable {
	/**
	 * Default undefined material.
	 */
	Material NONE = new Material() {
		@Override
		public RenderQueue queue() {
			throw new IllegalStateException();
		}

		@Override
		public Renderable texture() {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 * @return Render queue for this material
	 */
	RenderQueue queue();

	/**
	 * @return Texture
	 */
	Renderable texture();
}
