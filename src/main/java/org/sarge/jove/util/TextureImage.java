package org.sarge.jove.util;

import java.nio.ByteBuffer;

import org.sarge.jove.common.Dimensions;

/**
 * Wrapper for a texture image.
 * TODO - how to ensure image is released?
 * @author Sarge
 */
public interface TextureImage {
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
