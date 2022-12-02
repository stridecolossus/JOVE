package org.sarge.jove.platform.audio;

import static org.mockito.Mockito.*;
import static org.sarge.jove.platform.audio.AudioListener.DistanceModel.INVERSE;
import static org.sarge.jove.platform.audio.AudioParameter.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.NativeHelper.PointerToFloatArray;

class AudioListenerTest {
	private AudioListener listener;
	private AudioDevice dev;
	private AudioLibrary lib;

	@BeforeEach
	void before() {
		lib = mock(AudioLibrary.class);
		dev = mock(AudioDevice.class);
		when(dev.library()).thenReturn(lib);
		listener = new AudioListener(dev);
	}

	@Test
	void position() {
		final Point pos = new Point(1, 2, 3);
		listener.position(pos);
		verify(lib).alListener3f(POSITION, 1f, 2f, 3f);
	}

	@Test
	void velocity() {
		final Vector velocity = new Vector(1, 2, 3);
		listener.velocity(velocity);
		verify(lib).alListener3f(VELOCITY, 1f, 2f, 3f);
	}

	@Test
	void orientation() {
		final float[] array = {0, 0, 1, 0, 1, 0};
		listener.orientation(Axis.Z, Axis.Y);
		verify(lib).alListenerfv(ORIENTATION, new PointerToFloatArray(array));
	}

	@Test
	void gain() {
		listener.gain(2);
		verify(lib).alListenerf(GAIN, 2f);
	}

	@Test
	void doppler() {
		listener.doppler(2, 3);
		verify(lib).alDopplerFactor(2);
		verify(lib).alDopplerVelocity(3);
	}

	@Test
	void speed() {
		listener.speed(2);
		verify(lib).alSpeedOfSound(2);
	}

	@Test
	void model() {
		listener.model(INVERSE, true);
		verify(lib).alDistanceModel(0xD002);
	}
}
