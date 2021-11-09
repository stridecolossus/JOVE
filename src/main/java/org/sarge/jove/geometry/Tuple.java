package org.sarge.jove.geometry;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Component;
import org.sarge.jove.common.Layout;
import org.sarge.jove.util.MathsUtil;

/**
 * A <i>tuple</i> is the base-class for 3-component floating-point values.
 * @author Sarge
 */
sealed class Tuple implements Bufferable, Component permits Point, Vector {
	/**
	 * Size of a tuple.
	 */
	public static final int SIZE = 3;

	public final float x, y, z;

	/**
	 * Constructor.
	 */
	protected Tuple(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Copy constructor.
	 * @param tuple Tuple to copy
	 */
	protected Tuple(Tuple tuple) {
		this(tuple.x, tuple.y, tuple.z);
	}

	/**
	 * Array constructor.
	 * @param array Tuple array
	 * @throws IllegalArgumentException if the array is not comprised of three elements
	 */
	protected Tuple(float[] array) {
		if(array.length != SIZE) throw new IllegalArgumentException("Invalid array length: " + array.length);
		x = array[0];
		y = array[1];
		z = array[2];
	}

	/**
	 * Extracts a component from this tuple by index.
	 * @param index Component index 0..2
	 * @return Component of this tuple
	 * @throws IndexOutOfBoundsException for an invalid index
	 */
	public final float get(int index) {
		return switch(index) {
			case 0 -> x;
			case 1 -> y;
			case 2 -> z;
			default -> throw new IndexOutOfBoundsException("Invalid component index: " + index);
		};
		// TODO - enum instead of index?
	}

	/**
	 * Calculates the <i>dot</i> (or inner/scalar) product of this and the given tuple.
	 * <p>
	 * The dot product is a scalar that expresses the angular relationship between two values and is represented mathematically as:
	 * <p>
	 * <pre>A.B = |A| |B| cos(angle)</pre>
	 * <p>
	 * The dot product is:
	 * <ul>
	 * <li>zero if the vectors are orthogonal (i.e. perpendicular, or at right angles)</li>
	 * <li>greater than zero for an acute angle (less than 90 degree)</li>
	 * <li>negative if the angle is greater than 90 degrees</li>
	 * <li>commutative {@code a.b = b.a}</li>
	 * <li>equivalent to the cosine of the angle between two unit-vectors</li>
	 * </ul>
	 * <p>
	 * @param t Tuple
	 * @return Dot product
	 * @see <a href="https://en.wikipedia.org/wiki/Dot_product">Wikipedia</a>
	 */
	public final float dot(Tuple t) {
		return x * t.x + y * t.y + z * t.z;
	}

	@Override
	public Layout layout() {
		throw new RuntimeException();
	}

	@Override
	public final void buffer(ByteBuffer buffer) {
		buffer.putFloat(x).putFloat(y).putFloat(z);
	}

	@Override
	public final int hashCode() {
		return Objects.hash(x, y, z);
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Helper - Tests whether two tuples are equal.
	 * @param that Tuple
	 * @return Whether equal
	 */
	protected final boolean isEqual(Tuple that) {
		return
				MathsUtil.isEqual(this.x, that.x) &&
				MathsUtil.isEqual(this.y, that.y) &&
				MathsUtil.isEqual(this.z, that.z);
	}

	@Override
	public String toString() {
		return String.format("%s,%s,%s", x, y, z);
	}
}
