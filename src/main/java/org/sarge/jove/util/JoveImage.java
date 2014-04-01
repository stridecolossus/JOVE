package org.sarge.jove.util;

import java.nio.ByteBuffer;

import org.sarge.jove.common.Dimensions;

/**
 * Wrapper for an OpenGL format image.
 * TODO
 * - how to ensure image is released?
 * - different formats
 * - no point in RGB for grey-scale?
 * - 1D images?
 * @author Sarge
 */
public interface JoveImage {
	/**
	 * @return Image dimensions
	 */
	Dimensions getDimensions();

	/**
	 * @return Whether this image has an alpha channel
	 */
	boolean hasAlpha();

	/**
	 * @return Image as an NIO byte-buffer
	 */
	ByteBuffer getBuffer();
}
