package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Layout.Component;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * A <i>coordinate</i> is a 1, 2 or 3-dimensional texture coordinate.
 * @author Sarge
 */
public interface Coordinate extends Bufferable, Component {
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
		public Layout layout() {
			return LAYOUT;
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
		// TODO - wrong!?

		@Override
		public Layout layout() {
			return LAYOUT;
		}

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
					MathsUtil.isEqual(this.u, that.u) &&
					MathsUtil.isEqual(this.v, that.v);
		}

		/**
		 * A pair of <i>corners</i> define a texture coordinate rectangle.
		 */
		public record Corners(Coordinate2D topLeft, Coordinate2D bottomRight) {
			/**
			 * Constructor.
			 * @param topLeft			Top-left coordinate
			 * @param bottomRight		Bottom-right coordinate
			 */
			public Corners {
				Check.notNull(topLeft);
				Check.notNull(bottomRight);
			}

			/**
			 * Default constructor.
			 */
			public Corners() {
				this(TOP_LEFT, BOTTOM_RIGHT);
			}

//			/**
//			 * @return This pair of corners as a quad
//			 */
//			public Quad<Coordinate2D> quad() {
//				final var bottomLeft = new Coordinate2D(topLeft.u, bottomRight.v);
//				final var topRight = new Coordinate2D(bottomRight.u, topLeft.v);
//				return new Quad<>(List.of(topLeft, bottomLeft, topRight, bottomRight));
//			}
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
		public Layout layout() {
			return LAYOUT;
		}

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
					MathsUtil.isEqual(this.u, that.u) &&
					MathsUtil.isEqual(this.v, that.v) &&
					MathsUtil.isEqual(this.w, that.w);
		}
	}
}
