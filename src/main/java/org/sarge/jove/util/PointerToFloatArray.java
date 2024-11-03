package org.sarge.jove.util;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import com.sun.jna.Memory;

/**
 * JNA pointer-to-float-array.
 */
public class PointerToFloatArray extends Memory {
	private final float[] array;

	public PointerToFloatArray(float[] array) {
		super(array.length * Float.BYTES);
		this.array = requireNonNull(array);
		for(int n = 0; n < array.length; ++n) {
			setFloat(n * Float.BYTES, array[n]);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof PointerToFloatArray that) &&
				Arrays.equals(this.array, that.array);
	}
}