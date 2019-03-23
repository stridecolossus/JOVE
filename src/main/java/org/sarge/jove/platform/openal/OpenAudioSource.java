package org.sarge.jove.platform.openal;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.control.Player;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.platform.AudioService;
import org.sarge.jove.platform.AudioService.Buffer;
import org.sarge.jove.platform.AudioService.Range;
import org.sarge.jove.platform.Resource;
import org.sarge.jove.platform.openal.OpenAudioService.OpenAudioBuffer;

import com.sun.jna.ptr.FloatByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * OpenAL audio source.
 * @author Sarge
 */
class OpenAudioSource implements AudioService.Source {
	private static final Range PITCH = new Range(0.5f, 2f);

	private final OpenAudioLibrary lib;
	private final int src;
	private final Resource.Tracker<OpenAudioSource> tracker;

	private Range gain;

	/**
	 * Constructor.
	 * @param lib 			OpenAL library
	 * @param tracker		Tracker for sources
	 */
	OpenAudioSource(OpenAudioLibrary lib, Resource.Tracker<OpenAudioSource> tracker) {
		this.lib = notNull(lib);
		this.src = create(lib);
		this.tracker = notNull(tracker);
		tracker.add(this);
	}

	/**
	 * @return Source handle
	 */
	private static int create(OpenAudioLibrary lib) {
		// Generate source
		final PointerByReference ref = new PointerByReference();
		lib.alGenSources(1, ref);
		OpenAudioLibrary.check(lib);

		// Extract handle
		final int[] array = ref.getPointer().getIntArray(0, 1);
		return array[0];
	}

	@Override
	public void bind(Buffer buffer) {
		final OpenAudioBuffer open = (OpenAudioBuffer) buffer;
		lib.alSourcei(src, 0x1009, open.handle);
		OpenAudioLibrary.check(lib);
	}

	@Override
	public void apply(Player.State state) {
		switch(state) {
		case PLAY:
			lib.alSourcePlay(src);
			break;

		case PAUSE:
			lib.alSourcePause(src);
			break;

		case STOP:
			lib.alSourceStop(src);
			break;
		}
		OpenAudioLibrary.check(lib);
	}

	@Override
	public boolean isPlaying() {
		final IntByReference ref = new IntByReference();
		lib.alGetSourcei(src, 0x1010, ref);
		OpenAudioLibrary.check(lib);
		return ref.getValue() == 0x1012;
	}

	@Override
	public void setRepeating(boolean repeat) {
		lib.alSourcei(src, 0x1007, repeat ? 1 : 0);
		OpenAudioLibrary.check(lib);
	}

	@Override
	public void position(Point pos) {
		lib.alSourcefv(src, 0x1004, pos.toArray());
		OpenAudioLibrary.check(lib);
	}

	@Override
	public void direction(Vector dir) {
		lib.alSourcefv(src, 0x1005, dir.toArray());
		OpenAudioLibrary.check(lib);
	}

	@Override
	public void velocity(Vector velocity) {
		lib.alSourcefv(src, 0x1006, velocity.toArray());
		OpenAudioLibrary.check(lib);
	}

	@Override
	public Range gain() {
		if(gain == null) {
			gain = new Range(gain(0x100D), gain(0x100E));
		}
		return gain;
	}

	private float gain(int param) {
		final FloatByReference ref = new FloatByReference();
		lib.alGetSourcef(src, param, ref);
		OpenAudioLibrary.check(lib);
		return ref.getValue();
	}

	@Override
	public void gain(float gain) {
		final Range range = gain();
		if(!range.contains(gain)) throw new IllegalArgumentException(String.format("Invalid gain: actual=%f range=%s", gain, range));
		lib.alSourcef(src, 0x100A, gain);
		OpenAudioLibrary.check(lib);
	}

	@Override
	public Range pitch() {
		return PITCH;
	}

	@Override
	public void pitch(float pitch) {
		if(!PITCH.contains(pitch)) throw new IllegalArgumentException(String.format("Invalid pitch: actual=%f range=%s", pitch, PITCH));
		lib.alSourcef(src, 0x1003, pitch);
		OpenAudioLibrary.check(lib);
	}

	@Override
	public void destroy() {
		lib.alDeleteSources(1, new int[]{src});
		OpenAudioLibrary.check(lib);
		tracker.remove(this);
	}
}
