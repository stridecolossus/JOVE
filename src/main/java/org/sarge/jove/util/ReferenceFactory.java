package org.sarge.jove.util;

import com.sun.jna.ptr.*;

/**
 * The <i>reference factory</i> is used to generate pass-by-reference arguments for native API methods.
 * @author Sarge
 */
public class ReferenceFactory {
	/**
	 * @return New integer-by-reference
	 */
	public IntByReference integer() {
		return new IntByReference();
	}

	/**
	 * @return New pointer-by-reference
	 */
	public PointerByReference pointer() {
		return new PointerByReference();
	}
}
