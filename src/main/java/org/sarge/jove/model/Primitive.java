package org.sarge.jove.model;

/**
 * Drawing primitives.
 * @author Sarge
 * TODO - triangle fan?
 */
public enum Primitive {
	/**
	 * Triangles.
	 */
	TRIANGLE(3),

	/**
	 * Strip of triangles.
	 */
	TRIANGLE_STRIP(3),

	/**
	 * Quad.
	 */
	QUAD(4),

	/**
	 * Points.
	 */
	POINT(1),

	/**
	 * Lines.
	 */
	LINE(2),

	/**
	 * Strip of lines.
	 */
	LINE_STRIP(2);

	private final int size;

	/**
	 * Constructor.
	 * @param size Number of vertices per primitive
	 */
	private Primitive(int size) {
		this.size = size;
	}

	/**
	 * @return Number of vertices per primitive
	 */
	public int size() {
		return size;
	}

	/**
	 * @return Whether this primitive is a strip
	 */
	public boolean isStrip() {
		switch(this) {
		case TRIANGLE_STRIP:
		case LINE_STRIP:
			return true;

		default:
			return false;
		}
	}

	/**
	 * @return Whether this primitive supports face normals
	 */
	public boolean hasNormals() {
		switch(this) {
		case TRIANGLE:
		case TRIANGLE_STRIP:
		case QUAD:
			return true;

		default:
			return false;
		}
	}

	/**
	 * @param count Number of vertices
	 * @return Whether the given number of vertices is valid for this primitive
	 */
	public boolean isValidVertexCount(int count) {
		if(isStrip()) {
			return (count == 0) || (count >= size);
		}
		else {
			return (count % size) == 0;
		}
	}
}
