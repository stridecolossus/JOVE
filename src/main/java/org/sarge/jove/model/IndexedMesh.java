package org.sarge.jove.model;

/**
 * An <i>indexed mesh</i> has polygons specified by an additional index.
 * @author Sarge
 */
public interface IndexedMesh extends Mesh {
	/**
	 * @return Whether the index data is <i>compact</i>, i.e. comprised of {@code short} values rather than {@code int}
	 */
	boolean isCompactIndex();

	/**
	 * @return Index data
	 */
	DataBuffer index();
}
