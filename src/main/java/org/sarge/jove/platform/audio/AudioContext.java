package org.sarge.jove.platform.audio;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.*;

/**
 * An <i>audio context</i> is an active instance of a {@link AudioDevice}.
 * @author Sarge
 */
public class AudioContext extends TransientNativeObject {
	/**
	 * Creates a context for the given device.
	 * @param dev Device
	 * @return Context
	 * @throws RuntimeException if the context cannot be created
	 */
	public static AudioContext create(AudioDevice dev) {
		final AudioLibrary lib = dev.library();
		final Handle handle = lib.alcCreateContext(dev, new int[0]);
		if(handle == null) throw new RuntimeException("Cannot create context: " + dev);
		return new AudioContext(handle, dev);
	}

	private final AudioDevice dev;
	private final Library lib;

	/**
	 * Constructor.
	 * @param handle		Context handle
	 * @param dev			Device
	 */
	private AudioContext(Handle handle, AudioDevice dev) {
		super(handle);
		this.dev = requireNonNull(dev);
		this.lib = dev.library();
	}

	/**
	 * @return Audio device
	 */
	public AudioDevice device() {
		return dev;
	}

	/**
	 * Sets this as the current context.
	 */
	public void setCurrent() {
		lib.alcMakeContextCurrent(this);
	}

	@Override
	protected void release() {
		lib.alcDestroyContext(this);
	}

	/**
	 * OpenAL context library.
	 */
	interface Library {
		/**
		 * Creates a context.
		 * @param dev			Audio device
		 * @param attrlist		Attributes
		 * @return Context
		 */
		Handle alcCreateContext(AudioDevice dev, int[] attrlist);

		/**
		 * Sets the context current.
		 * @param ctx Context
		 * @return Result
		 */
		boolean alcMakeContextCurrent(AudioContext ctx);

		/**
		 * Destroys a context.
		 * @param ctx Context
		 */
		void alcDestroyContext(AudioContext ctx);
	}
}
