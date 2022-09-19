package org.sarge.jove.scene;

import java.util.stream.Stream;

/**
 * A <i>scene graph</i> defines a recursive group of nodes that can be rendered.
 * @author Sarge
 */
public interface SceneGraph {
	/**
	 * @return Scene graph nodes
	 */
	Stream<SceneGraph> nodes();
}
