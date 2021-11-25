package org.sarge.jove.io;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;

/**
 * An <i>image data</i> is an abstraction for a general image comprising multiple array layers and MIP levels.
 * <p>
 * The image {@link #components} specifies the order of the channels comprising the image, e.g. {@code ABGR} for a transparent native image.
 * <p>
 * The {@link #layout} describes the number of channels comprising the image and the structure of each pixel.
 * For example a transparent image with one byte per channel would have the following layout: <code>new Layout(4, Byte.class, 1, false)</code>
 * <p>
 * @author Sarge
 */
public interface ImageData {
	/**
	 * @return Image dimensions
	 */
	Dimensions size();

	/**
	 * @return Components
	 */
	String components();

	/**
	 * @return Component layout
	 */
	Layout layout();

	/**
	 * @return Number of layers
	 */
	int layers();

	/**
	 * @return Number of MIP levels
	 */
	int levels();

	/**
	 * Retrieves the image data for the given layer and MIP level.
	 * @param layer		Layer
	 * @param level		MIP level
	 * @return Image data
	 */
	Bufferable data(int layer, int level);
}
