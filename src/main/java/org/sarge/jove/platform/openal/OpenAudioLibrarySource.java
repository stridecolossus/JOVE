package org.sarge.jove.platform.openal;

import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * OpenAL source API.
 * @author Sarge
 */
interface OpenAudioLibrarySource {
	/**
	 * Creates sources.
	 * @param size			Number to create
	 * @param sources		Returned handle(s)
	 */
	void alGenSources(int size, PointerByReference sources);

	/**
	 * Deletes sources.
	 * @param size			Number to delete
	 * @param sources		Source handle(s)
	 */
	void alDeleteSources(int size, int[] sources);

	/**
	 * Retrieves an integer source property.
	 * @param src			Source
	 * @param param			Parameter
	 * @param result		Returned result
	 */
	void alGetSourcei(int src, int param, IntByReference result);

	/**
	 * Retrieves a floating-point source property.
	 * @param src			Source
	 * @param param			Parameter
	 * @param result		Returned result
	 */
	void alGetSourcef(int src, int param, FloatByReference result);

	/**
	 * Sets an integer source property.
	 * @param src			Source
	 * @param param			Parameter
	 * @param value			Value
	 */
	void alSourcei(int src, int param, int value);

	/**
	 * Sets a floating-point source property.
	 * @param src			Source
	 * @param param			Parameter
	 * @param value			Value
	 */
	void alSourcef(int src, int param, float value);

	/**
	 * Sets a floating-point array property.
	 * @param src			Source
	 * @param param			Parameter
	 * @param array			Array
	 */
	void alSourcefv(int src, int param, float[] array);

	/**
	 * Plays the given source.
	 * @param src Source
	 */
	void alSourcePlay(int src);

	/**
	 * Pauses the given source.
	 * @param src Source
	 */
	void alSourcePause(int src);

	/**
	 * Stop playing the given source.
	 * @param src Source
	 */
	void alSourceStop(int src);

	/**
	 * Rewinds the given source.
	 * @param src Source
	 */
	void alSourceRewind(int src);
}
