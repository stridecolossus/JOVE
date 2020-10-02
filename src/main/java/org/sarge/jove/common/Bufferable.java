package org.sarge.jove.common;

import java.nio.FloatBuffer;

/**
 * A <i>bufferable</i> object can be stored to a floating-point buffer.
 * @author Sarge
 */
@FunctionalInterface
public interface Bufferable {
	/**
	 * Writes this object to the given buffer.
	 * @param buffer Buffer
	 */
	void buffer(FloatBuffer buffer);

//	/**
//	 * Populates an existing float-buffer with the given bufferable array.
//	 * @param fb Float-buffer
//	 * @param array Array to buffer
//	 * @throws NullPointerException if any array element is <tt>null</tt>
//	 */
//	static void populate(FloatBuffer fb, Bufferable... array) {
//		populate(fb, 0, array);
//	}
//
//	/**
//	 * Populates an existing float-buffer with the given bufferable array at the specified buffer offset.
//	 * @param fb 		Float-buffer
//	 * @param offset	Offset into buffer
//	 * @param array Array to buffer
//	 * @throws NullPointerException if any array element is <tt>null</tt>
//	 */
//	static void populate(FloatBuffer fb, int offset, Bufferable... array) {
//		fb.position(offset);
//		for(Bufferable b : array) {
//			b.buffer(fb);
//		}
//	}
}
