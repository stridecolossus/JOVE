package org.sarge.jove.model;

import java.io.IOException;

import org.sarge.jove.scene.NodeGroup;
import org.sarge.jove.util.Loader;

/**
 * Model loader definition.
 * TODO - is it viable to define this? what about use of rendering system? split into two steps?
 * @author Sarge
 */
public interface ModelLoader extends Loader<NodeGroup> {
	/**
	 * Loads a model from the given path.
	 * @param path File path
	 * @return Model scene-graph
	 * @throws IOException if the model file cannot be opened or is invalid
	 */
	NodeGroup load( String path ) throws IOException;
}
