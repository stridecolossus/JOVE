package org.sarge.jove.util;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import com.sun.jna.Memory;

/**
 * JNA pointer-to-integer-array.
 */
public class PointerToIntArray extends Memory {
	private final int[] array;

	public PointerToIntArray(int[] array) {
		super(array.length * Integer.BYTES);
		this.array = requireNonNull(array);
		for(int n = 0; n < array.length; ++n) {
			setInt(n * Integer.BYTES, array[n]);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof PointerToIntArray that) &&
				Arrays.equals(this.array, that.array);
	}
}