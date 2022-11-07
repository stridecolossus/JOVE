package org.sarge.jove.util;

import java.util.Arrays;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * JNA memory wrapper for a pointer array.
 * @author Sarge
 */
public class PointerArray extends Memory {
	private final Pointer[] array;

	/**
	 * Constructor.
	 * @param array Pointer array
	 */
	public PointerArray(Pointer[] array) {
		super(Native.POINTER_SIZE * array.length);
		this.array = array;
		for(int n = 0; n < array.length; ++n) {
			setPointer(n * Native.POINTER_SIZE, array[n]);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof PointerArray that) &&
				Arrays.equals(this.array, that.array);
	}
}
