package org.sarge.jove.common;

import org.sarge.jove.model.Vertex;

/**
 * A <i>vertex component</i> is a composite bufferable object that can be composed into vertex data.
 * @see Vertex
 * @author Sarge
 */
public interface VertexComponent extends Bufferable {
	/**
	 * @return Layout of this component
	 */
	Layout layout();

	@Override
	default int length() {
		return this.layout().length();
	}
}
