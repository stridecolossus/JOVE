package org.sarge.jove.model;

import static org.sarge.lib.util.Check.zeroOrMore;

/**
 * Drawing primitives.
 * @author Sarge
 */
public enum Primitive {
	POINTS(1),
	PATCH(1),
	LINES(2),
	LINE_STRIP(2),
	TRIANGLES(3),
	TRIANGLE_STRIP(3),
	TRIANGLE_FAN(3) {
		@Override
		public int faces(int count) {
			return Math.max(0, count - 2);
		}

		@Override
		public int[] indices(int face) {
			final int[] indices = new int[3];
			indices[0] = face;
			indices[1] = face + 1;
			indices[2] = 0;
			return indices;
		}
	};

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
	 * @return Whether this primitive is a triangular polygon that implicitly supports a vertex normal
	 */
	public boolean isTriangle() {
		return switch(this) {
			case TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> true;
			default -> false;
		};
	}

	/**
	 * Determines the number of faces for the given draw count.
	 * Returns zero or less for an invalid draw count for this primitive.
	 * @param count Draw count
	 * @return Number of faces
	 */
	public int faces(int count) {
		if(isStrip()) {
			return count - (size - 1);
		}
		else {
			return count / size;
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

	/**
	 * Generates the vertices indices for a polygon of this primitive.
	 * @param face Face index
	 * @return Vertex indices
	 */
	public int[] indices(int face) {
		final int[] indices = new int[size];
		final int start = isStrip() ? face : face * size;
		for(int n = 0; n < size; ++n) {
			indices[n] = start + n;
		}
		return indices;
	}
}
