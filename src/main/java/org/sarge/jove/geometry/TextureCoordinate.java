package org.sarge.jove.geometry;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>texture coordinate</i> is a 1D, 2D or 3D coordinate.
 */
public interface TextureCoordinate extends Bufferable {
	/**
	 * @return Texture coordinates as an array
	 */
	float[] toArray();

	/**
	 * Creates a texture coordinate from the given array.
	 * @param array Array
	 * @return Texture coordinate
	 * @throws IllegalArgumentException if the array length is not in the range 1..3
	 */
	static TextureCoordinate of(float[] array) {
		switch(array.length) {
		case 1:		return new Coordinate1D(array[0]);
		case 2:		return new Coordinate2D(array[0], array[1]);
		case 3:		return new Coordinate3D(array[0], array[1], array[2]);
		default:	throw new IllegalArgumentException("Invalid array length for texture coordinate: " + Arrays.toString(array));
		}
	}

	/**
	 * One-dimensional texture coordinate.
	 */
	class Coordinate1D implements TextureCoordinate {
		public final float u;

		/**
		 * Constructor.
		 * @param u
		 */
		public Coordinate1D(float u) {
			this.u = u;
		}

		@Override
		public float[] toArray() {
			return new float[]{u};
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof Coordinate1D that) && MathsUtil.isEqual(this.u, that.u);
		}

		@Override
		public final String toString() {
			return Arrays.toString(toArray());
		}
	}

	/**
	 * Two-dimensional texture coordinate.
	 */
	class Coordinate2D extends Coordinate1D {
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

		/**
		 * Default texture coordinates for a quad (counter-clockwise winding order).
		 */
		public static final List<Coordinate2D> QUAD = List.of(TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT);

		public final float v;

		/**
		 * Constructor.
		 * @param u
		 * @param v
		 */
		public Coordinate2D(float u, float v) {
			super(u);
			this.v = v;
		}

		/**
		 * Array constructor.
		 * @param array Array
		 */
		public Coordinate2D(float[] array) {
			this(array[0], array[1]);
		}

		@Override
		public float[] toArray() {
			return new float[]{u, v};
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u).putFloat(v);
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
	class Coordinate3D extends Coordinate2D {
		public final float w;

		/**
		 * Constructor.
		 * @param u
		 * @param v
		 * @param w
		 */
		public Coordinate3D(float u, float v, float w) {
			super(u, v);
			this.w = w;
		}

		@Override
		public float[] toArray() {
			return new float[]{u, v, w};
		}

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u).putFloat(v).putFloat(w);
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
