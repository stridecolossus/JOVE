package org.sarge.jove.platform.openal;

import static org.sarge.lib.util.Check.notNull;

import java.io.FileInputStream;

import org.sarge.jove.control.Player;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.platform.AudioData;
import org.sarge.jove.platform.AudioService;
import org.sarge.jove.platform.Resource;
import org.sarge.lib.util.Util;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * OpenAL implementation.
 * @author Sarge
 */
public class OpenAudioService implements AudioService {
	/**
	 * Creates the OpenAL audio service.
	 * @return OpenAL service
	 */
	public static OpenAudioService create() {
		final OpenAudioLibrary lib = OpenAudioLibrary.create();
		return new OpenAudioService(lib);
	}

	/**
	 * Maps a generic audio format descriptor to the OpenAL format identifier.
	 * @throws UnsupportedOperationException if the audio format is not supported by this platform
	 */
	private static int format(AudioData.Format format) {
		final boolean stereo = format.channels() > 1;
		switch(format.samples()) {
		case 8:		return stereo ? 0x1102 : 0x1100;
		case 16:	return stereo ? 0x1103 : 0x1101;
		default:	throw new UnsupportedOperationException("Audio format not supported: " + format);
		}
	}

	/**
	 * OpenAL buffer.
	 */
	class OpenAudioBuffer implements Buffer {
		final int handle;

		/**
		 * Constructor.
		 * @param handle Buffer handle
		 * TODO - factor out common code from here and AudioSource
		 */
		OpenAudioBuffer() {
			// Generate buffer
			final PointerByReference ref = new PointerByReference();
			lib.alGenBuffers(1, ref);
			OpenAudioLibrary.check(lib);

			// Extract handle
			final int[] array = ref.getPointer().getIntArray(0, 1);
			this.handle = array[0];

			// Register
			buffers.add(this);
		}

		@Override
		public void load(AudioData data) {
			final int format = format(data.format());
			final byte[] array = data.data();
			lib.alBufferData(handle, format, array, array.length, data.format().rate());
			OpenAudioLibrary.check(lib);
		}

		@Override
		public void destroy() {
			lib.alDeleteBuffers(1, new int[]{handle});
			OpenAudioLibrary.check(lib);
			buffers.remove(this);
		}
	}

	/**
	 * OpenAL listener.
	 */
	class OpenAudioListener implements Listener {
		@Override
		public void position(Point pos) {
			lib.alListener3f(0x1004, pos.x, pos.y, pos.z);
			OpenAudioLibrary.check(lib);
		}

		@Override
		public void velocity(Vector velocity) {
			lib.alListener3f(0x1006, velocity.x, velocity.y, velocity.z);
			OpenAudioLibrary.check(lib);
		}

		@Override
		public void orientation(Vector view, Vector up) {
			final float[] v = view.toArray();
			final float[] u = up.toArray();
			final float[] array = new float[2 * 3];
			System.arraycopy(v, 0, array, 0, 3);
			System.arraycopy(u, 0, array, 3, 3);
			lib.alListenerfv(0x100F, array);
			OpenAudioLibrary.check(lib);
		}

		@Override
		public void gain(float gain) {
			lib.alListenerf(0x100A, gain);
			OpenAudioLibrary.check(lib);
		}
	}

	private final OpenAudioLibrary lib;
	private final Pointer device;
	private final Pointer context;
	private final OpenAudioListener listener = new OpenAudioListener();
	private final Resource.Tracker<OpenAudioBuffer> buffers = new Resource.Tracker<>();
	private final Resource.Tracker<OpenAudioSource> sources = new Resource.Tracker<>();

	/**
	 * Constructor.
	 * @param lib OpenAL library
	 */
	OpenAudioService(OpenAudioLibrary lib) {
		// Open library
		this.lib = notNull(lib);

		// Open audio device
		this.device = lib.alcOpenDevice(null);
		OpenAudioLibrary.check(lib);

		// Create context
		this.context = lib.alcCreateContext(device, null);
		OpenAudioLibrary.check(lib);
		lib.alcMakeContextCurrent(context);
		OpenAudioLibrary.check(lib);
	}

	@Override
	public String name() {
		return "OpenAL";
	}

	@Override
	public void handler(ErrorHandler handler) {
	}

	@Override
	public String version() {
		return version(0x1000) + "." + version(0x1001);
	}

	/**
	 * Looks up a version number.
	 */
	private int version(int param) {
		final IntByReference ver = new IntByReference();
		lib.alcGetIntegerv(device, param, 1, ver);
		return ver.getValue();
	}

	@Override
	public Source source() {
		return new OpenAudioSource(lib, sources);
	}

	@Override
	public Buffer buffer() {
		return new OpenAudioBuffer();
	}

	@Override
	public Listener listener() {
		return listener;
	}

	@Override
	public void close() {
		// Destroy allocated resources
		sources.destroy();
		buffers.destroy();

		// Close device
		lib.alcCloseDevice(device);
		OpenAudioLibrary.check(lib);

		// Destroy context
		lib.alcDestroyContext(context);
		OpenAudioLibrary.check(lib);
	}

	// https://www.gamedev.net/articles/programming/general-and-gameplay-programming/a-guide-to-starting-with-openal-r2008/
	// https://www.gamedev.net/forums/topic/452383-how-to-load-ogg-files/

	public static void main(String[] args) throws Exception {
		System.out.println("Initialising...");
		final OpenAudioService service = OpenAudioService.create();
		System.out.println("version="+service.version());
		System.out.println("device="+service.device);
		System.out.println("context="+service.context);

		System.out.println("Creating source...");
		final Source src = service.source();
		System.out.println("source="+src);
		System.out.println("gain="+src.gain());
		System.out.println("pitch="+src.pitch());

		System.out.println("Creating buffer...");
		final Buffer buffer = service.buffer();
		System.out.println("buffer="+buffer);

		System.out.println("Loading audio...");
		final AudioData.Loader loader = new AudioData.Loader();
		final AudioData audio = loader.load(new FileInputStream("./src/test/resources/Footsteps.wav"));
		System.out.println("audio="+audio);

		System.out.println("Loading buffer...");
		buffer.load(audio);

		System.out.println("Binding...");
		src.bind(buffer);

		System.out.println("Playing...");
		src.apply(Player.State.PLAY);

		while(src.isPlaying()) {
			Util.kip(100L);
		}

		System.out.println("Destroying...");
		src.destroy();
		buffer.destroy();
		service.close();
	}
}
