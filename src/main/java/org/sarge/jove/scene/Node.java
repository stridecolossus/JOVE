package org.sarge.jove.scene;

import org.sarge.jove.geometry.*;

/**
 * A <i>node</i> is an element of a scene graph.
 * @author Sarge
 */
public interface Node {
	/**
	 * @return Local transform of this node
	 */
	Transform transform();

	/**
	 * @return World matrix of this node
	 */
	Matrix matrix();

	/**
	 * @return Bounding volume of this node
	 */
	Volume volume();
}
