package org.sarge.jove.model;

/**
 * Drawing primitives.
 * @author Sarge
 */
public enum Primitive {
	/**
	 * Triangles.
	 */
	TRIANGLES(3),

	/**
	 * Strip of triangles.
	 */
	TRIANGLE_STRIP(3),

	/**
	 * Triangle fan.
	 */
	TRIANGLE_FAN(3),

	/**
	 * Points.
	 */
	POINTS(1),

	/**
	 * Lines.
	 */
	LINES(2),

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
		return switch(this) {
		case TRIANGLE_STRIP, TRIANGLE_FAN, LINE_STRIP -> true;
		default -> false;
		};
	}

	/**
	 * @return Whether this primitive supports face normals
	 */
	public boolean hasNormals() {
		return switch(this) {
		case TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> true;
		default -> false;
		};
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
