package org.sarge.jove.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;

import com.sun.jna.*;

/**
 * JNA pointer-to-pointer-array.
 */
public class PointerToPointerArray extends Memory {
	private final Pointer[] array;

	public PointerToPointerArray(Pointer[] array) {
		super(array.length * Native.POINTER_SIZE);
		this.array = notNull(array);
		for(int n = 0; n < array.length; ++n) {
			setPointer(n * Native.POINTER_SIZE, array[n]);
		}
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof PointerToPointerArray that) &&
				Arrays.equals(this.array, that.array);
	}
}