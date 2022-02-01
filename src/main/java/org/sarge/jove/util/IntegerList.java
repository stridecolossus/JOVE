package org.sarge.jove.util;

import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.lib.util.Check;

/**
 * An <i>integer list</i> is a simple mutable list abstraction for primitive integers.
 * <p>
 * Only a minimal set of operations are provided rather than implementing the whole {@link List} interface.
 * <p>
 * This class is <b>not</b> thread-safe.
 * <p>
 * An integer list can be written to an NIO buffer using the {@link #buffer(IntBuffer)} method.
 * <p>
 * A list can be written as {@code short} values via {@link #buffer(ShortBuffer)} to support (for example) a <i>small</i> index buffer.
 * Note that values are silently truncated to {@code short} integers.
 * <p>
 * @author Sarge
 */
public class IntegerList {
	/**
	 * Maximum length of a {@code short} buffer.
	 */
	public static final long SHORT = MathsUtil.unsignedMaximum(Short.SIZE);

	private int[] array;
	private int size;
	private final float growth;

	/**
	 * Constructor with default initial capacity and growth factor.
	 */
	public IntegerList() {
		this(128, 1);
	}

	/**
	 * Constructor.
	 * @param capacity		Initial capacity
	 * @param growth		Growth factor
	 * @throws IllegalArgumentException if the growth factor is zero or negative
	 */
	public IntegerList(int capacity, float growth) {
		Check.oneOrMore(capacity);
		if(growth <= 0) throw new IllegalArgumentException("Growth factor must be positive");
		this.array = new int[capacity];
		this.growth = growth;
	}

	/**
	 * @return Size of this list
	 */
	public int size() {
		return size;
	}

	/**
	 * @return Capacity of this list
	 */
	int capacity() {
		return array.length;
	}

	/**
	 * @return This list as a stream of integers
	 */
	public IntStream stream() {
		return Arrays.stream(array, 0, size);
	}

	/**
	 * Copies a <i>slice</i> of this list to the given array.
	 * @param offset		Offset
	 * @param out			Output array
	 * @throws ArrayIndexOutOfBoundsException if the offset and output array exceeds the size of this list
	 */
	public void slice(int offset, int[] out) {
		if(offset + out.length > size) throw new ArrayIndexOutOfBoundsException(String.format("Invalid slice: offset=%d len=%d size=%d", offset, out.length, size));
		System.arraycopy(array, offset, out, 0, out.length);
	}

	/**
	 * Adds an integer growing the list as required.
	 * @param n Integer to add
	 */
	public void add(int n) {
		if(size == array.length) {
			final int inc = Math.max(1, (int) (size * growth));
			final int[] next = new int[size + inc];
			System.arraycopy(array, 0, next, 0, size);
			array = next;
		}

		array[size] = n;
		++size;
	}

	/**
	 * Clears this list.
	 */
	public void clear() {
		size = 0;
	}

	/**
	 * Copies this list of integers to the given NIO buffer.
	 * @param buffer Buffer
	 * @see #SHORT
	 */
	public void buffer(IntBuffer buffer) {
		if(buffer.isDirect()) {
			for(int n : array) {
				buffer.put(n);
			}
		}
		else {
			buffer.put(array, 0, size);
		}
	}

	/**
	 * Copies this list to the given NIO buffer as <b>short</i> integer values.
	 * Note that values are silently truncated to {@code short} integers.
	 * @param buffer Buffer
	 * @see #SHORT
	 */
	public void buffer(ShortBuffer buffer) {
		for(int n = 0; n < size; ++n) {
			buffer.put((short) array[n]);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(array);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) &&
				(obj instanceof IntegerList that) &&
				(this.size == that.size) &&
				Arrays.equals(this.array, that.array);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("size", size)
				.append("capacity", array.length)
				.build();
	}
}
