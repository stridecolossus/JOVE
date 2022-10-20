package org.sarge.jove.platform.util;

import java.util.Arrays;

import com.sun.jna.Memory;

/**
 * JNA memory wrapper for a pointer to a floating-point array.
 * @author Sarge
 */
public class FloatArray extends Memory {
	private final float[] array;

	/**
	 * Constructor.
	 * @param array Float array
	 */
	public FloatArray(float[] array) {
		super(Float.BYTES * array.length);
		this.array = array;
		for(int n = 0; n < array.length; ++n) {
			setFloat(n * Float.BYTES, array[n]);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof FloatArray that) &&
				Arrays.equals(this.array, that.array);
	}
}
