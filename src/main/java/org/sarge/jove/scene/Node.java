package org.sarge.jove.scene;

import org.sarge.jove.geometry.*;

/**
 * A <i>node</i> is an element of a scene graph.
 * @author Sarge
 */
public interface Node {
//	/**
//	 * @return World matrix of this node
//	 */
//	Matrix matrix();

	/**
	 * @return Transform of this node
	 */
	Transform transform();

	/**
	 * @return Bounding volume of this node
	 */
	Volume volume();
}
