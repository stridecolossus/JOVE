package org.sarge.jove.util;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.lib.util.Check;

/**
 * An <i>integer list</i> is a simple mutable list abstraction for primitive integers.
 * <p>
 * Notes:
 * <ul>
 * <li>only a minimal set of operations are provided rather than implementing the whole {@link List} interface</li>
 * <li>this class is <b>not</b> thread-safe</li>
 * <li>an integer list can also be written to an NIO buffer using the {@link #bufferable()} converter</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public class IntegerList {
	private int[] array;
	private int size;
	private float growth;

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
	 * @throws IllegalArgumentException if the growth factor is zero or not positive
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
	 * Converts this list to a bufferable object.
	 * Note that changes to the list are reflected in the bufferable object.
	 * @return Bufferable list
	 */
	public Bufferable bufferable() {
		return new Bufferable() {
			@Override
			public int length() {
				return size * Integer.BYTES;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				final IntBuffer buffer = bb.asIntBuffer();
				if(buffer.isDirect()) {
					for(int n = 0; n < size; ++n) {
						buffer.put(array[n]);
					}
				}
				else {
					buffer.put(array, 0, size);
				}
			}
		};
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

	@Override
	public int hashCode() {
		return Objects.hash(array, size);
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
		return new ToStringBuilder(this).append("size", size).build();
	}
}
