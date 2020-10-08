package org.sarge.jove.util;

import com.sun.jna.Function.PostCallRead;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;

/**
 * Wrapper class for an array-of-pointers.
 * @author Sarge
 * @see StringArray
 */
public class PointerArray extends Memory implements PostCallRead {
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
	public void read() {
		read(0, array, 0, array.length);
	}
}
