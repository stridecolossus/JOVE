package org.sarge.jove.common;

/**
 * A <i>component</i> is a bufferable object with a layout.
 * @author Sarge
 */
public interface Component extends Bufferable {
	/**
	 * @return Layout of this component
	 */
	Layout layout();

	@Override
	default int length() {
		return this.layout().length();
	}
}