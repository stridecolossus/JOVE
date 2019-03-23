package org.sarge.jove.common;

import java.nio.FloatBuffer;

import org.sarge.jove.util.BufferFactory;

/**
 * A <i>bufferable</i> object can be stored to a floating-point buffer.
 * @author Sarge
 */
public interface Bufferable {
	/**
	 * @return Size of this bufferable object
	 */
	int size();

	/**
	 * Writes this object to the given buffer.
	 * @param buffer Buffer
	 */
	void buffer(FloatBuffer buffer);

	/**
	 * Creates and populates a float-buffer containing the given bufferable array.
	 * @param array Array to buffer
	 * @return New float-buffer
	 * @throws NullPointerException if any array element is <tt>null</tt>
	 */
	static FloatBuffer create(Bufferable... array) {
		// Handle empty buffer
		if(array.length == 0) {
			return BufferFactory.floatBuffer(0);
		}

		// Create buffer
		final int size = array[0].size();
		final FloatBuffer fb = BufferFactory.floatBuffer(size * array.length);

		// Populate buffer
		populate(fb, array);
		fb.flip();
		return fb;
	}

	/**
	 * Populates an existing float-buffer with the given bufferable array.
	 * @param fb Float-buffer
	 * @param array Array to buffer
	 * @throws NullPointerException if any array element is <tt>null</tt>
	 */
	static void populate(FloatBuffer fb, Bufferable... array) {
		populate(fb, 0, array);
	}

	/**
	 * Populates an existing float-buffer with the given bufferable array at the specified buffer offset.
	 * @param fb 		Float-buffer
	 * @param offset	Offset into buffer
	 * @param array Array to buffer
	 * @throws NullPointerException if any array element is <tt>null</tt>
	 */
	static void populate(FloatBuffer fb, int offset, Bufferable... array) {
		fb.position(offset);
		for(Bufferable b : array) {
			b.buffer(fb);
		}
	}
}
