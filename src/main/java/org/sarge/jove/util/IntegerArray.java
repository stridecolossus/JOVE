package org.sarge.jove.util;

import java.util.Arrays;

import com.sun.jna.Memory;

/**
 * JNA memory wrapper for an integer array.
 * @author Sarge
 */
public class IntegerArray extends Memory {
	private final int[] array;

	/**
	 * Constructor.
	 * @param array Integer array
	 */
	public IntegerArray(int[] array) {
		super(Integer.BYTES * array.length);
		this.array = array;
		for(int n = 0; n < array.length; ++n) {
			setInt(n * Integer.BYTES, array[n]);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof IntegerArray that) &&
				Arrays.equals(this.array, that.array);
	}
}
