package org.sarge.jove.texture;

import java.nio.FloatBuffer;
import java.util.Arrays;

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
	 * Creates a one-dimensional texture coordinate.
	 * @param u
	 * @return 1D texture coordinate
	 */
	static TextureCoordinate of(float u) {
		return new Coordinate1D(u);
	}

	/**
	 * Creates a two-dimensional texture coordinate.
	 * @param u
	 * @param v
	 * @return 2D texture coordinate
	 */
	static TextureCoordinate of(float u, float v) {
		return new Coordinate2D(u, v);
	}

	/**
	 * Creates a three-dimensional (or cube) texture coordinate.
	 * @param u
	 * @param v
	 * @param w
	 * @return Texture cube coordinate
	 */
	static TextureCoordinate of(float u, float v, float w) {
		return new Coordinate3D(u, v, w);
	}

	/**
	 * Creates a texture coordinate from the given array.
	 * @param array Array
	 * @return Texture coordinate
	 * @throws IllegalArgumentException if the array length is not in the range 1..3
	 */
	static TextureCoordinate of(float[] array) {
		switch(array.length) {
		case 1:		return of(array[0]);
		case 2:		return of(array[0], array[1]);
		case 3:		return of(array[0], array[1], array[2]);
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
		private Coordinate1D(float u) {
			this.u = u;
		}

		@Override
		public int size() {
			return 1;
		}

		@Override
		public float[] toArray() {
			return new float[]{u};
		}

		@Override
		public final void buffer(FloatBuffer buffer) {
			buffer.put(toArray());
		}

		@Override
		public final boolean equals(Object obj) {
			if(obj == this) return true;
			if(obj == null) return false;
			if(obj instanceof TextureCoordinate) {
				final TextureCoordinate that = (TextureCoordinate) obj;
				final float[] a = this.toArray();
				final float[] b = that.toArray();
				for(int n = 0; n < a.length; ++n) {
					if(!MathsUtil.equals(a[n], b[n])) return false;
				}
				return true;
			}
			else {
				return false;
			}
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
		public final float v;

		/**
		 * Constructor.
		 * @param u
		 * @param v
		 */
		private Coordinate2D(float u, float v) {
			super(u);
			this.v = v;
		}

		@Override
		public int size() {
			return 2;
		}

		@Override
		public float[] toArray() {
			return new float[]{u, v};
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
		private Coordinate3D(float u, float v, float w) {
			super(u, v);
			this.w = w;
		}

		@Override
		public int size() {
			return 3;
		}

		@Override
		public float[] toArray() {
			return new float[]{u, v, w};
		}
	}
}
