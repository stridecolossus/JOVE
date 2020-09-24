package org.sarge.jove.platform.vulkan;

import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * The <i>reference factory</i> is used to generate pass-by-reference arguments for Vulkan API methods.
 * @author Sarge
 */
public interface ReferenceFactory {
	/**
	 * @return New pointer-by-reference
	 */
	PointerByReference pointer();

	/**
	 * @return New integer-by-reference
	 */
	IntByReference integer();

	/**
	 * Default implementation.
	 */
	ReferenceFactory DEFAULT = new ReferenceFactory() {
		@Override
		public PointerByReference pointer() {
			return new PointerByReference();
		}

		@Override
		public IntByReference integer() {
			return new IntByReference();
		}
	};
}
