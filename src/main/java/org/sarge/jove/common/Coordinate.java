package org.sarge.jove.common;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>coordinate</i> is a 1, 2 or 3-dimensional texture coordinate.
 * @author Sarge
 */
public interface Coordinate extends VertexComponent {
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
	 * @param size Number of texture coordinates (1..3)
	 * @return Texture coordinate layout
	 * @throws IllegalArgumentException if the given size is invalid
	 */
	static Layout layout(int size) {
		Check.range(size, 1, 3);
		return Coordinate1D.LAYOUTS[size - 1];
	}

	/**
	 * One-dimensional texture coordinate.
	 */
	record Coordinate1D(float u) implements Coordinate {
		private static final Layout[] LAYOUTS = IntStream.range(1, 4).mapToObj(Layout::of).toArray(Layout[]::new);

		@Override
		public Layout layout() {
			return LAYOUTS[0];
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || ((obj instanceof Coordinate1D that) && MathsUtil.isEqual(this.u, that.u));
		}
	}

	/**
	 * Two-dimensional texture coordinate.
	 */
	record Coordinate2D(float u, float v) implements Coordinate {
		/**
		 * Quad coordinates.
		 */
		public static final Coordinate2D
			TOP_LEFT		= new Coordinate2D(0, 0),
			BOTTOM_LEFT 	= new Coordinate2D(0, 1),
			TOP_RIGHT 		= new Coordinate2D(1, 0),
			BOTTOM_RIGHT 	= new Coordinate2D(1, 1);

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u).putFloat(v);
		}

		@Override
		public Layout layout() {
			return Coordinate1D.LAYOUTS[1];
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
		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u).putFloat(v).putFloat(w);
		}

		@Override
		public Layout layout() {
			return Coordinate1D.LAYOUTS[2];
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
