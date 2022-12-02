package org.sarge.jove.util;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;

import com.sun.jna.*;

/**
 * JNA helpers and utilities.
 * <p>
 * The various pointer-to-array implementations are convenience wrappers for a contiguous JNA memory block that also supports equality for testing purposes.
 * <p>
 * @author Sarge
 */
public final class NativeHelper {
	private NativeHelper() {
	}

	/**
	 * JNA pointer-to-integer-array.
	 */
	public static class PointerToIntArray extends Memory {
		private final int[] array;

		public PointerToIntArray(int[] array) {
			super(array.length * Integer.BYTES);
			this.array = notNull(array);
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

	/**
	 * JNA pointer-to-float-array.
	 */
	public static class PointerToFloatArray extends Memory {
		private final float[] array;

		public PointerToFloatArray(float[] array) {
			super(array.length * Float.BYTES);
			this.array = notNull(array);
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

	/**
	 * JNA pointer-to-pointer-array.
	 */
	public static class PointerToPointerArray extends Memory {
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
}
