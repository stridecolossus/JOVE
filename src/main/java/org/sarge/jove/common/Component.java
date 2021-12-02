package org.sarge.jove.common;

/**
 * A <i>component</i> is a bufferable object with a specific {@link Layout}, such as a vertex position, texture coordinate, etc.
 * @author Sarge
 */
public interface Component extends Bufferable {
	/**
	 * @return Layout of this component
	 */
	Layout layout();
}
