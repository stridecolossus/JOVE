package org.sarge.jove.platform.vulkan.util;

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
	};
}
