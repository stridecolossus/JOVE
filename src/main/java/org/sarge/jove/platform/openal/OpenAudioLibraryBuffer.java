package org.sarge.jove.platform.openal;

import com.sun.jna.ptr.PointerByReference;

/**
 * OpenAL buffer API.
 * @author Sarge
 */
interface OpenAudioLibraryBuffer {
	/**
	 * Creates audio buffers.
	 * @param size			Number of buffers
	 * @param buffers		Returned buffer handle(s)
	 */
	void alGenBuffers(int size, PointerByReference buffers);

	/**
	 * Copies data to a buffer.
	 * @param buffer		Buffer handle
	 * @param format		Data format
	 * @param data			Data
	 * @param size			Size
	 * @param rate			Sample rate
	 */
	void alBufferData(int buffer, int format, byte[] data, int size, int rate);

	/**
	 * Destroys audio buffers.
	 * @param size			Number of buffers
	 * @param buffers		Buffer handle(s)
	 */
	void alDeleteBuffers(int size, int[] buffers);
}
