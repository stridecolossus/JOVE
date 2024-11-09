package org.sarge.jove.lib;

/**
 * The <i>reference factory</i> generates by-reference types used by native API methods.
 * @author Sarge
 */
public interface ReferenceFactory {
	/**
	 * @return Integer-by-reference
	 */
	IntegerReference integer();

	/**
	 * @return Pointer-by-reference
	 */
	PointerReference pointer();
}
