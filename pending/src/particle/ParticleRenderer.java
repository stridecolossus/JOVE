package org.sarge.jove.particle;

import java.util.List;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.scene.RenderContext;

/**
 * Renderer for a {@link ParticleSystem}.
 * @author Sarge
 */
public interface ParticleRenderer {
	/**
	 * Initialises this renderer.
	 * @param sys Rendering system
	 */
	void init( RenderingSystem sys );

	/**
	 * Renders particles.
	 * @param particles		Particles to render
	 * @param ctx			Rendering context
	 */
	void render( List<Particle> particles, RenderContext ctx );
}
