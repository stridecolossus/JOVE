package org.sarge.jove.scene;

import java.util.Optional;

import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Model;

/**
 * A <i>renderable</i> defines an object that can be rendered.
 * @author Sarge
 */
public interface Renderable {
	/**
	 * @return Renderable model
	 */
	Model model();

	/**
	 * @return Material
	 */
	Optional<Material> material();

	/**
	 * @return World matrix
	 */
	Matrix matrix();

	/**
	 * @return Bounding volume
	 */
	Volume volume();
}
