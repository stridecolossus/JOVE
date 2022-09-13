package org.sarge.jove.common;

/**
 * A <i>byte sized</i> defines a data type that has a <i>stride</i> (or length in bytes) such as matrices, image pixels, vertex components, etc.
 * @author Sarge
 */
public interface ByteSized {
	/**
	 * @return Stride (or length) of this data
	 */
	int stride();
}
