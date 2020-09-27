package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * The <i>reference factory</i> is used to generate pass-by-reference arguments for Vulkan API methods.
 * @author Sarge
 */
public interface ReferenceFactory {
	/**
	 * @return New integer-by-reference
	 */
	IntByReference integer();

	/**
	 * @return New pointer-by-reference
	 */
	PointerByReference pointer();

	/**
	 * @param num Array size
	 * @return New pointer array
	 */
	Pointer[] pointers(int num);

	/**
	 * Default implementation.
	 */
	ReferenceFactory DEFAULT = new ReferenceFactory() {
		@Override
		public IntByReference integer() {
			return new IntByReference();
		}

		@Override
		public PointerByReference pointer() {
			return new PointerByReference();
		}

		@Override
		public Pointer[] pointers(int num) {
			return new Pointer[num];
		}
	};
}
