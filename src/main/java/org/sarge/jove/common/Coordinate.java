package org.sarge.jove.common;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Coordinate.*;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>coordinate</i> is a 1, 2 or 3-dimensional texture coordinate.
 * @author Sarge
 */
public sealed interface Coordinate extends Bufferable permits Coordinate1D, Coordinate2D, Coordinate3D {
	/**
	 * Creates a texture coordinate from the given array.
	 * @param array Array
	 * @return Texture coordinate
	 * @throws IllegalArgumentException if the array length is not in the range 1..3
	 */
	static Coordinate of(float[] array) {
		return switch(array.length) {
			case 1 ->	new Coordinate1D(array[0]);
			case 2 ->	new Coordinate2D(array[0], array[1]);
			case 3 ->	new Coordinate3D(array[0], array[1], array[2]);
			default ->	throw new IllegalArgumentException("Invalid array length for texture coordinate: " + Arrays.toString(array));
		};
	}

	/**
	 * One-dimensional texture coordinate.
	 */
	record Coordinate1D(float u) implements Coordinate {
		/**
		 * Layout of a 1D texture coordinate.
		 */
		public static final Layout LAYOUT = Layout.floats(1);

		@Override
		public int length() {
			return Float.BYTES;
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Coordinate1D that) &&
					MathsUtil.isEqual(this.u, that.u);
		}
	}

	/**
	 * Two-dimensional texture coordinate.
	 */
	record Coordinate2D(float u, float v) implements Coordinate {
		/**
		 * Layout of a 2D texture coordinate.
		 */
		public static final Layout LAYOUT = Layout.floats(2);

		/**
		 * Quad coordinates.
		 */
		public static final Coordinate2D
			TOP_LEFT		= new Coordinate2D(0, 0),
			BOTTOM_LEFT 	= new Coordinate2D(0, 1),
			TOP_RIGHT 		= new Coordinate2D(1, 0),
			BOTTOM_RIGHT 	= new Coordinate2D(1, 1);

		/**
		 * Texture coordinates for a quad with a counter-clockwise winding order.
		 */
		public static final List<Coordinate2D> QUAD = List.of(TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT);

		@Override
		public int length() {
			return 2 * Float.BYTES;
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u).putFloat(v);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Coordinate2D that) &&
					MathsUtil.isEqual(this.u, that.u) &&
					MathsUtil.isEqual(this.v, that.v);
		}
	}

	/**
	 * Three-dimensional texture coordinate.
	 */
	record Coordinate3D(float u, float v, float w) implements Coordinate {
		/**
		 * Layout of a 3D texture coordinate.
		 */
		public static final Layout LAYOUT = Layout.floats(3);

		@Override
		public int length() {
			return 3 * Float.BYTES;
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u).putFloat(v).putFloat(w);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Coordinate3D that) &&
					MathsUtil.isEqual(this.u, that.u) &&
					MathsUtil.isEqual(this.v, that.v) &&
					MathsUtil.isEqual(this.w, that.w);
		}
	}
}
