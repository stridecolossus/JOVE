package org.sarge.jove.widget;

/**
 * Widget border.
 * @author Sarge
 */
public interface Border {
	/**
	 * @return Border insets
	 */
	Dimensions getDimensions();
	
	/**
	 * Renders this border.
	 */
	void render();
}
