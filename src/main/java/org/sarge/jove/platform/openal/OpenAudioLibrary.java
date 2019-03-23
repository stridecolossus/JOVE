package org.sarge.jove.platform.openal;

import org.sarge.jove.platform.Service.ServiceException;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * OpenAL API.
 * @author Sarge
 */
interface OpenAudioLibrary extends Library, OpenAudioLibrarySource, OpenAudioLibraryBuffer, OpenAudioLibraryListener {
	/**
	 * Extension name for enumeration of devices.
	 */
	String ENUMERATE_EXTENSIONS = "ALC_ENUMERATION_EXT";

	//chrome-extension://oemmndcbldboiebfnladdacbdfmadadm/https://www.openal.org/documentation/OpenAL_Programmers_Guide.pdf
	// https://ffainelli.github.io/openal-example/

	/**
	 * Creates the OpenAL API.
	 * @return OpenAL API
	 */
	static OpenAudioLibrary create() {
		return Native.load("OpenAL32", OpenAudioLibrary.class);
	}

	/**
	 * @throws ServiceException if the most recent OpenAL method returned an error
	 */
	static void check(OpenAudioLibrary lib) {
		// TODO - alGetString(error) -> message
		// TODO - delegate to service error handler, but how to pass to OpenAudioSource?
		final int error = lib.alGetError();
		if(error != 0) {
			throw new ServiceException("OpenAL error: " + error);
		}
	}

	/**
	 * @return Most recent error code
	 */
	int alGetError();

	/**
	 * Retrieves an OpenAL string.
	 * @param device	Device (optional)
	 * @param param 	Parameter
	 * @return String
	 */
	String alcGetString(Pointer device, int param);

	/**
	 * Tests whether the given extension is supported.
	 * @param name Extension name
	 * @return Whether extension is supported
	 */
	boolean alIsExtensionPresent(String name);

	/**
	 * Opens a device.
	 * @param name Device name or <tt>null<tt> for the default device
	 * @return Device handle
	 */
	Pointer alcOpenDevice(String name);

	/**
	 * Retrieves a device configuration parameter.
	 * @param device		Device
	 * @param param			Parameter
	 * @param size			Size of returned result
	 * @param result		Result
	 * @return
	 */
	void alcGetIntegerv(Pointer device, int param, int size, IntByReference result);

	/**
	 * Destroys a device.
	 * @param context Device to destroy
	 */
	void alcCloseDevice(Pointer device);

	/**
	 * Creates a context.
	 * @param device			Device
	 * @param attributes		Attributes list (optional)
	 * @return Context
	 */
	Pointer alcCreateContext(Pointer device, String attributes);

	/**
	 * Destroys a context.
	 * @param context Context to destroy
	 */
	void alcDestroyContext(Pointer context);

	/**
	 * Sets the current context.
	 * @param context Context
	 */
	void alcMakeContextCurrent(Pointer context);
}
