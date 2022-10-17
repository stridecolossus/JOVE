package org.sarge.jove.model;

import static org.sarge.lib.util.Check.zeroOrMore;

/**
 * Drawing primitives.
 * @author Sarge
 */
public enum Primitive {
	POINTS(1),
	LINES(2),
	LINE_STRIP(2),
	TRIANGLES(3),
	TRIANGLE_STRIP(3),
	TRIANGLE_FAN(3),
	PATCH(1);

	private final int size;

	/**
	 * Constructor.
	 * @param size Number of vertices per primitive
	 */
	private Primitive(int size) {
		this.size = zeroOrMore(size);
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
	public boolean isNormalSupported() {
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
