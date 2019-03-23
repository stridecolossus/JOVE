package org.sarge.jove.platform.openal;

/**
 * OpenAL listener API.
 * @author Sarge
 */
public interface OpenAudioLibraryListener {
	/**
	 * Sets a floating-point listener property.
	 * @param param 	Parameter
	 * @param f			Value
	 */
	void alListenerf(int param, float f);

	/**
	 * Sets a floating-point tuple property.
	 * @param param Parameter
	 * @param x
	 * @param y
	 * @param z
	 */
	void alListener3f(int param, float x, float y, float z);

	/**
	 * Sets a floating-point array property.
	 * @param param		Parameter
	 * @param array		Array
	 */
	void alListenerfv(int param, float[] array);
}
