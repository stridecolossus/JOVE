package org.sarge.jove.lib;

/**
 * The <i>reference factory</i> generates by-reference types used by native API methods.
 * <p>
 * This factory approach is used to support effective testing or native methods that make use of by-reference types.
 * <p>
 * @author Sarge
 */
public class ReferenceFactory {
	/**
	 * @return Integer-by-reference
	 */
	public PointerReference pointer() {
		return new PointerReference();
	}

	/**
	 * @return Pointer-by-reference
	 */
	public IntegerReference integer() {
		return new IntegerReference();
	}
}
