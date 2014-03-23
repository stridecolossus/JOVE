package org.sarge.jove.model;

/**
 * Triangle.
 * @author Sarge
 */
public interface Triangle {
	/**
	 * @param idx Vertex index 0..2
	 * @return Triangle vertex
	 */
	Vertex getVertex( int idx );
}
