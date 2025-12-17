package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.sarge.jove.common.*;
import org.sarge.jove.util.MathsUtility;

/**
 * A <i>coordinate</i> is a 1, 2 or 3-dimensional texture coordinate.
 * @author Sarge
 */
public sealed interface Coordinate extends Bufferable {
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
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Coordinate1D that) &&
					MathsUtility.isApproxEqual(this.u, that.u);
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

		@Override
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u);
			buffer.putFloat(v);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Coordinate2D that) &&
					MathsUtility.isApproxEqual(this.u, that.u) &&
					MathsUtility.isApproxEqual(this.v, that.v);
		}

		/**
		 * A pair of <i>corners</i> defines a texture coordinate rectangle.
		 */
		public record Corners(Coordinate2D topLeft, Coordinate2D bottomRight) {
			/**
			 * Constructor.
			 * @param topLeft			Top-left coordinate
			 * @param bottomRight		Bottom-right coordinate
			 */
			public Corners {
				requireNonNull(topLeft);
				requireNonNull(bottomRight);
			}

			/**
			 * Default constructor.
			 */
			public Corners() {
				this(TOP_LEFT, BOTTOM_RIGHT);
			}
		}
		// TODO - only used by glyphs?
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
		public void buffer(ByteBuffer buffer) {
			buffer.putFloat(u);
			buffer.putFloat(v);
			buffer.putFloat(w);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Coordinate3D that) &&
					MathsUtility.isApproxEqual(this.u, that.u) &&
					MathsUtility.isApproxEqual(this.v, that.v) &&
					MathsUtility.isApproxEqual(this.w, that.w);
		}
	}
}
