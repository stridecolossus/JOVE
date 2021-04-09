package org.sarge.jove.geometry;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>texture coordinate</i> is a 1D, 2D or 3D coordinate.
 */
public interface Coordinate extends Bufferable {
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
		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u);
		}

		@Override
		public long length() {
			return 1 * Float.BYTES;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Coordinate1D that) && MathsUtil.isEqual(this.u, that.u);
		}
	}

	/**
	 * Two-dimensional texture coordinate.
	 */
	record Coordinate2D(float u, float v) implements Coordinate {
		/**
		 * Size of 2D coordinates.
		 */
		public static final int SIZE = 2;

		/**
		 * Quad coordinates.
		 */
		public static final Coordinate2D
			TOP_LEFT		= new Coordinate2D(0, 0),
			BOTTOM_LEFT 	= new Coordinate2D(0, 1),
			TOP_RIGHT 		= new Coordinate2D(1, 0),
			BOTTOM_RIGHT 	= new Coordinate2D(1, 1);

		// TODO
		public static Coordinate2D of(float[] array) {
			return (Coordinate2D) Coordinate.of(array);
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u).putFloat(v);
		}

		@Override
		public long length() {
			return 2 * Float.BYTES;
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj instanceof Coordinate2D that) &&
					MathsUtil.isEqual(this.u, that.u) &&
					MathsUtil.isEqual(this.v, that.v);
		}
	}

	/**
	 * Three-dimensional texture coordinate.
	 */
	record Coordinate3D(float u, float v, float w) implements Coordinate {
		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u).putFloat(v).putFloat(w);
		}

		@Override
		public long length() {
			return 3 * Float.BYTES;
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj instanceof Coordinate3D that) &&
					MathsUtil.isEqual(this.u, that.u) &&
					MathsUtil.isEqual(this.v, that.v) &&
					MathsUtil.isEqual(this.w, that.w);
		}
	}
}
