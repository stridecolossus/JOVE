package org.sarge.jove.platform.audio;

import static org.sarge.jove.platform.audio.AudioParameter.*;
import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.jove.common.*;

import com.sun.jna.Pointer;

/**
 * An <i>audio buffer</i> contains data to be played by a {@link AudioSource}.
 * <p>
 * Note that this buffer can only be destroyed if it is not referenced by any source, see {@link AudioSource#clear()}.
 * <p>
 * @author Sarge
 */
public class AudioBuffer extends TransientNativeObject {
	/**
	 * Creates an audio buffer.
	 * @param dev Audio device
	 * @return New audio buffer
	 */
	public static AudioBuffer create(AudioDevice dev) {
    	final Library lib = dev.library();
    	final int[] id = new int[1];
    	lib.alGenBuffers(1, id);
    	dev.check();
		return new AudioBuffer(new Handle(new Pointer(id[0])), dev);
	}

	private final AudioDevice dev;

	/**
	 * Constructor.
	 * @param handle		Buffer handle
	 * @param dev			Audio device
	 */
	private AudioBuffer(Handle handle, AudioDevice dev) {
		super(handle);
		this.dev = notNull(dev);
	}

	/**
	 * Loads audio data into this buffer.
	 * @param audio Audio data
	 * @throws IllegalArgumentException if the given {@link #audio} format is not supported
	 */
	public void load(Audio audio) {
    	final AudioParameter format = format(audio);
    	final byte[] data = audio.data();
    	final Library lib = dev.library();
    	lib.alBufferData(this, format.value(), data, data.length, audio.frequency());
    	dev.check();
    }

	/**
	 * Maps the given audio to the corresponding OpenAL format.
	 * @param audio Audio
	 * @return OpenAL format
	 */
	private static AudioParameter format(Audio audio) {
		final boolean stereo = switch(audio.channels()) {
			case 1 -> false;
			case 2 -> true;
			default -> throw new IllegalArgumentException("Invalid number of channels: " + audio);
		};

		return switch(audio.bitsPerSample()) {
			case 8 -> stereo ? STEREO_8 : MONO_8;
			case 16 -> stereo ? STEREO_16 : MONO_16;
			default -> throw new IllegalArgumentException("Unsupported OpenAL format: " + audio);
		};
	}

	@Override
	protected void release() {
    	final Library lib = dev.library();
    	final Pointer buffers = NativeObject.array(List.of(this));
    	lib.alDeleteBuffers(1, buffers);
    	dev.check();
	}

	/**
	 * Open AL buffer API.
	 */
	interface Library {
		/**
		 * Creates a group of audio buffers.
		 * @param count			Number of buffers to create
		 * @param buffers		Returned buffer handles
		 */
		void alGenBuffers(int count, int[] buffers);

		/**
		 * Deletes a group of audio buffers.
		 * @param count			Number of buffers to delete
		 * @param buffers		Buffer handles
		 */
		void alDeleteBuffers(int n, Pointer buffers);

		/**
		 * Loads the given audio data to this buffer.
		 * @param buffer		Audio buffer
		 * @param format		Format
		 * @param data			Audio data
		 * @param size			Length of the data (bytes)
		 * @param freq			Frequency (or sample rate)
		 */
		void alBufferData(AudioBuffer buffer, int format, byte[] data, int size, int freq);
	}
}
