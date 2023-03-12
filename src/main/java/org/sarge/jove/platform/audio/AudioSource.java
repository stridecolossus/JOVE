package org.sarge.jove.platform.audio;

import static org.sarge.jove.platform.audio.AudioParameter.*;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.control.Playable;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * An <i>audio source</i> plays an {@link AudioBuffer}.
 * @author Sarge
 */
public class AudioSource extends TransientNativeObject {
	/**
	 * Creates an audio source.
	 * @param dev Audio device
	 * @return New audio source
	 */
	public static AudioSource create(AudioDevice dev) {
		final Library lib = dev.library();
		final int[] ids = new int[1];
		lib.alGenSources(1, ids);
		dev.check();
		return new AudioSource(new Handle(new Pointer(ids[0])), dev);
	}

	protected final AudioDevice dev;
	protected final Library lib;
	protected final List<AudioBuffer> buffers = new ArrayList<>();
	private final AudioSourcePlayable playable = new AudioSourcePlayable();

	/**
	 * Constructor.
	 * @param handle		Source handle
	 * @param dev			Audio device
	 */
	private AudioSource(Handle handle, AudioDevice dev) {
		super(handle);
		this.dev = notNull(dev);
		this.lib = dev.library();
	}

	/**
	 * Copy constructor.
	 * @param src Source to copy
	 */
	protected AudioSource(AudioSource src) {
		this(src.handle, src.dev);
	}

	/**
	 * @return Device
	 */
	public AudioDevice device() {
		return dev;
	}

	/**
	 * @return Audio buffer(s)
	 */
	public Stream<AudioBuffer> buffers() {
		return buffers.stream();
	}

	/**
	 * Sets the audio buffer to be played by this source.
	 * @param buffer Audio buffer
	 */
	public void buffer(AudioBuffer buffer) {
		Check.notNull(buffer);
		lib.alSourcei(this, BUFFER, buffer);
		dev.check();
		buffers.add(buffer);
		playable.stop();
	}

	/**
	 * Clears all buffers in this source.
	 */
	public void clear() {
		lib.alSourcei(this, BUFFER, 0);
		dev.check();
		buffers.clear();
		playable.stop();
	}

	/**
	 *
	 */
	private class AudioSourcePlayable extends Playable {
		@Override
		public boolean isPlaying() {
    		final var ref = new IntByReference();
    		lib.alGetSourcei(AudioSource.this, SOURCE_STATE, ref);
    		return ref.getValue() == PLAYING.value();
		}

		@Override
		public void apply(State state) {
			super.apply(state);
			switch(state) {
        		case PLAY -> {
        			if(buffers.isEmpty()) throw new IllegalStateException("No buffer(s) to play: " + this);
        			lib.alSourcePlay(AudioSource.this);
        		}
        		case PAUSE -> {
        			lib.alSourcePause(AudioSource.this);
        		}
        		case STOP -> {
        			lib.alSourceStop(AudioSource.this);
        		}
    		}
			dev.check();
		}

		/**
		 * Interrupts this audio source if playing.
		 */
		private void stop() {
			if(this.state() != State.STOP) {
				apply(State.STOP);
			}
		}
	}

	/**
	 * @return This source as a playable instance
	 */
	public Playable playable() {
		return playable;
	}

	/**
	 * Sets the pitch of this source.
	 * @param pitch Pitch
	 */
	public void pitch(float pitch) {
		lib.alSourcef(this, PITCH, pitch);
		dev.check();
	}

	/**
	 * Sets the gain of this source.
	 * @param gain Gain
	 */
	public void gain(float gain) {
		lib.alSourcef(this, GAIN, gain);
		dev.check();
	}

	/**
	 * Sets whether this source is looping.
	 * @param loop Whether looping
	 */
	public void loop(boolean loop) {
		lib.alSourcei(this, LOOPING, loop);
		dev.check();
	}

	/**
	 * Sets the position of this source.
	 * @param pos Source position
	 */
	public void position(Point pos) {
		Check.notNull(pos);
		lib.alSource3f(this, POSITION, pos.x, pos.y, pos.z);
		dev.check();
	}

	/**
	 * Sets the direction of this source.
	 * @param dir Source direction
	 */
	public void direction(Vector dir) {
		Check.notNull(dir);
		lib.alSource3f(this, DIRECTION, dir.x, dir.y, dir.z);
		dev.check();
	}

	/**
	 * Sets the velocity of this source.
	 * @param velocity Source velocity
	 */
	public void velocity(Vector velocity) {
		Check.notNull(velocity);
		lib.alSource3f(this, VELOCITY, velocity.x, velocity.y, velocity.z);
		dev.check();
	}

	/**
	 * Rewinds this source.
	 */
	public void rewind() {
		lib.alSourceRewind(this);
		dev.check();
	}
	// TODO - is this valid for a queue?

	@Override
	protected void release() {
    	final Library lib = dev.library();
    	final Pointer sources = NativeObject.array(List.of(this));
    	lib.alDeleteSources(1, sources);
    	dev.check();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(playable)
				.append("buffers", buffers.size())
				.build();
	}

	/**
	 * OpenAL source API.
	 */
	interface Library {
		void alGenSources(int n, int[] sources);
		void alDeleteSources(int n, Pointer sources);

		void alGetSourcei(AudioSource source, AudioParameter param, IntByReference value);
		void alSourcef(AudioSource source, AudioParameter param, float value);
		void alSource3f(AudioSource source, AudioParameter param, float value1, float value2, float value3);
		void alSourcei(AudioSource source, AudioParameter param, boolean value);
		void alSourcei(AudioSource source, AudioParameter param, int value);
		void alSourcei(AudioSource source, AudioParameter param, AudioBuffer buffer);

		void alSourcePlay(AudioSource source);
		void alSourcePause(AudioSource source);
		void alSourceStop(AudioSource source);
		void alSourceRewind(AudioSource source);

		void alGetSourceiv(AudioSource source, AudioParameter param, int[] values); // PointerByReference values);
		void alSourceQueueBuffers(AudioSource source, int numEntries, Pointer buffers);
		void alSourceUnqueueBuffers(AudioSource source, int numEntries, Pointer buffers);
	}

// TODO
//	 * Min Gain                          AL_MIN_GAIN             ALfloat
//	 * Max Gain                          AL_MAX_GAIN             ALfloat
//	 * Head Relative Mode                AL_SOURCE_RELATIVE      ALint (AL_TRUE or AL_FALSE)
//	 * Reference Distance                AL_REFERENCE_DISTANCE   ALfloat
//	 * Max Distance                      AL_MAX_DISTANCE         ALfloat
//	 * RollOff Factor                    AL_ROLLOFF_FACTOR       ALfloat
//	 * Inner Angle                       AL_CONE_INNER_ANGLE     ALint or ALfloat
//	 * Outer Angle                       AL_CONE_OUTER_ANGLE     ALint or ALfloat
//	 * Cone Outer Gain                   AL_CONE_OUTER_GAIN      ALint or ALfloat
//	 * MS Offset                         AL_MSEC_OFFSET          ALint or ALfloat
//	 * Byte Offset                       AL_BYTE_OFFSET          ALint or ALfloat
//	 * Sample Offset                     AL_SAMPLE_OFFSET        ALint or ALfloat
}
