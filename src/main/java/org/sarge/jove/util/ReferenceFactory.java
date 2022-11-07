package org.sarge.jove.util;

import com.sun.jna.ptr.*;

/**
 * The <i>reference factory</i> is used to generate pass-by-reference arguments for native API methods.
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
