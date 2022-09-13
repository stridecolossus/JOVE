package org.sarge.jove.common;

/**
 * A <i>stride</i> defines a data type that has a <i>stride</i> (or length in bytes) such as matrices, image pixels, vertex components, etc.
 * @author Sarge
 */
public interface Stride {
	/**
	 * @return Stride (or length) of this layout
	 */
	int stride();
}
