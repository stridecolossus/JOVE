package org.sarge.jove.model;

import java.nio.Buffer;

import org.sarge.jove.common.GraphicResource;

/**
 * Base-interface for vertex and index buffers.
 * @author Sarge
 * @param <T> Buffer type
 */
interface BufferObject<T extends Buffer> extends GraphicResource {
	/**
	 * Binds this buffer object to the context.
	 */
	void activate();

	/**
	 * Uploads static data.
	 * @param data Data
	 * @param mode Buffer access mode
	 */
	void buffer( T data, AccessMode mode );

	/**
	 * Uploads dynamic data.
	 * @param data		Data
	 * @param start		Start index
	 */
	void buffer( T data, int start );

	/**
	 * Binds the default buffer object.
	 */
	void deactivate();
}
