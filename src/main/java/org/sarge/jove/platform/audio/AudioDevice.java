package org.sarge.jove.platform.audio;

import static java.util.Objects.requireNonNull;

import java.io.File;
import java.util.*;

import javax.sound.sampled.AudioSystem;

import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.platform.audio.AudioListener.DistanceModel;
import org.sarge.jove.platform.audio.AudioSource.AudioSourcePlayable;
import org.sarge.jove.util.IntEnum;

/**
 * An <i>audio device</i> represents a audio player on this system.
 * @author Sarge
 */
public class AudioDevice extends TransientNativeObject {
	private static final IntEnum.ReverseMapping<AudioParameter> ERRORS = IntEnum.reverse(AudioParameter.class);

	/**
	 * Creates an audio device.
	 * @param name		Device name or {@code null} for the default device
	 * @param lib		Audio library
	 * @return Device
	 */
	public static AudioDevice create(String name, AudioLibrary lib) {
		final Handle handle = lib.alcOpenDevice(name);
		if(handle == null) throw new RuntimeException("Error creating device");
		return new AudioDevice(handle, lib);
	}

	/**
	 * Enumerates the audio devices on this system.
	 * @param lib Audio library
	 * @return Devices
	 */
	public static List<AudioDevice> devices(AudioLibrary lib) {
		final String str = lib.alcGetString(null, AudioParameter.DEVICE_SPECIFIER);
		final List<String> parts = split(str); // TODO
		return parts.stream().map(dev -> create(dev, lib)).toList();
	}

	private static List<String> split(String str) {
		final List<String> list = new ArrayList<>();
		int prev = 0;
		for(int n = 0; n < str.length(); ++n) {
			if(str.charAt(n) == 0) {
				final String part = str.substring(prev, n);
				list.add(part);
				prev = n + 1;
			}
		}
		if(prev < str.length()) {
			final String part = str.substring(prev, str.length());
			list.add(part);
		}
		return list;
	}

	private final AudioLibrary lib;

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param lib			Audio library
	 */
	private AudioDevice(Handle handle, AudioLibrary lib) {
		super(handle);
		this.lib = requireNonNull(lib);
	}

	/**
	 * @return Audio library
	 */
	AudioLibrary library() {
		return lib;
	}

	/**
	 * @throws RuntimeException if an error occurs on this device
	 */
	public void check() {
		final String message = this.message();
		if(message != null) {
			throw new RuntimeException(message);
		}
	}

	/**
	 * Retrieves the latest OpenAL error.
	 * @return Latest error
	 */
	public Optional<String> error() {
		return Optional.ofNullable(message());
	}

	/**
	 * @return Latest error message or {@code null} if none
	 */
	private String message() {
		final int error = lib.alGetError();
		if(error == 0) {
			return null;
		}
		else {
			return message(error);
		}
	}

	/**
	 * Maps an error message.
	 */
	private static String message(int error) {
		try {
			return ERRORS.map(error).name();
		}
		catch(IllegalArgumentException e) {
			return "Unknown error: " + error;
		}
	}

	@Override
	protected void release() {
		final boolean ok = lib.alcCloseDevice(this);
		if(!ok) throw new RuntimeException("Error closing device: " + this);
	}

	/**
	 * Open AL device library.
	 */
	interface Library {
		/**
		 * Retrieves an audio system property.
		 * @param dev		Optional device
		 * @param param		Parameter
		 * @return Properties
		 */
		String alcGetString(AudioDevice dev, AudioParameter param);

		/**
		 * Opens a device.
		 * @param name Device name or {@code null} for the default device
		 * @return Device
		 */
		Handle alcOpenDevice(String name);

		/**
		 * Closes a device.
		 * @param dev Device
		 * @return Result
		 */
		boolean alcCloseDevice(AudioDevice dev);
	}


	public static void main(String[] args) throws Exception {
		//https://indiegamedev.net/2020/02/15/the-complete-guide-to-openal-with-c-part-1-playing-a-sound/

		System.out.println("Loading audio");
		final var loader = new Audio.Loader();
		final Audio one = loader.load(AudioSystem.getAudioInputStream(new File("/Users/Sarge/workspace/Demo/Data/iamtheprotectorofthissystem.wav")));
		final Audio two = loader.load(AudioSystem.getAudioInputStream(new File("/Users/Sarge/workspace/Demo/Data/Footsteps.wav")));

		System.out.println("Creating system");
		final AudioLibrary lib = AudioLibrary.create();

//		System.out.println("devices="+AudioDevice.devices(lib));

		System.out.println("Creating device");
		final AudioDevice dev = AudioDevice.create(null, lib);

		System.out.println("Creating context");
		final AudioContext ctx = AudioContext.create(dev);

		System.out.println("Make current");
		ctx.setCurrent();

		System.out.println("Initialising listener");
		final AudioListener listener = new AudioListener(dev);
		listener.position(Point.ORIGIN);
		//listener.doppler(2, 3);
		listener.speed(4);
		listener.model(DistanceModel.INVERSE, true);

		System.out.println("Creating buffers");
		final AudioBuffer bufferOne = AudioBuffer.create(dev);
		final AudioBuffer bufferTwo = AudioBuffer.create(dev);

		System.out.println("Buffering audio");
		bufferOne.load(one);
		bufferTwo.load(two);

		System.out.println("Creating source");
		final AudioSource src = AudioSource.create(dev);
		src.pitch(1);
		src.gain(1);
		src.position(Point.ORIGIN);
		src.velocity(new Vector(0, 0, 0));
		src.loop(false);

		System.out.println("Creating queue");
		final AudioQueue queue = new AudioQueue(src);
		queue.queue(List.of(bufferOne, bufferTwo));
		System.out.println(queue.buffers().toList());

		System.out.println("Playing...");
		final AudioSourcePlayable playable = queue.playable();
		playable.play();
		while(playable.isPlaying()) {
			Thread.sleep(50);
			queue.processed();
		}

		System.out.println("Cleaning up...");
		src.clear();
		bufferOne.destroy();
		bufferTwo.destroy();
		src.destroy();
		ctx.destroy();
		dev.destroy();
	}
}
