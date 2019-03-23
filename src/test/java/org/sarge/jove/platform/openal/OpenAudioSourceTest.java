package org.sarge.jove.platform.openal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.Player;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.platform.AudioService.Range;
import org.sarge.jove.platform.Resource;
import org.sarge.jove.platform.openal.OpenAudioService.OpenAudioBuffer;

public class OpenAudioSourceTest {
	private OpenAudioSource source;
	private OpenAudioLibrary lib;
	private Resource.Tracker<OpenAudioSource> tracker;

	@SuppressWarnings("unchecked")
	@BeforeEach
	public void before() {
		lib = mock(OpenAudioLibrary.class);
		tracker = mock(Resource.Tracker.class);
		source = new OpenAudioSource(lib, tracker);
	}

	@Test
	public void constructor() {
		assertEquals(false, source.isPlaying());
		verify(tracker).add(source);
	}

	@Test
	public void bind() {
		final OpenAudioBuffer buffer = mock(OpenAudioBuffer.class);
		source.bind(buffer);
		verify(lib).alSourcei(0, 0x1009, 0);
	}

	@Test
	public void play() {
		source.apply(Player.State.PLAY);
		verify(lib).alSourcePlay(0);
	}

	@Test
	public void pause() {
		source.apply(Player.State.PAUSE);
		verify(lib).alSourcePause(0);
	}

	@Test
	public void stop() {
		source.apply(Player.State.STOP);
		verify(lib).alSourceStop(0);
	}

	@Test
	public void setRepeating() {
		source.setRepeating(true);
		source.setRepeating(false);
		verify(lib).alSourcei(0, 0x1007, 1);
		verify(lib).alSourcei(0, 0x1007, 0);
	}

	@Test
	public void gainRange() {
		final Range gain = source.gain();
		assertNotNull(gain);
	}

	@Test
	public void gain() {
		source.gain(0);
		verify(lib).alSourcef(0, 0x100A, 0f);
	}

	@Test
	public void gainInvalid() {
		assertThrows(IllegalArgumentException.class, () -> source.gain(999));
	}

	@Test
	public void pitchRange() {
		assertEquals(new Range(0.5f, 2f), source.pitch());
	}

	@Test
	public void pitch() {
		source.pitch(1);
		verify(lib).alSourcef(0, 0x1003, 1f);
	}

	@Test
	public void pitchInvalid() {
		assertThrows(IllegalArgumentException.class, () -> source.pitch(999));
	}

	@Test
	public void position() {
		final Point pos = new Point(1, 2, 3);
		source.position(pos);
		verify(lib).alSourcefv(0, 0x1004, pos.toArray());
	}

	@Test
	public void direction() {
		final Vector dir = new Vector(1, 2, 3);
		source.direction(dir);
		verify(lib).alSourcefv(0, 0x1005, dir.toArray());
	}

	@Test
	public void velocity() {
		final Vector velocity = new Vector(1, 2, 3);
		source.velocity(velocity);
		verify(lib).alSourcefv(0, 0x1006, velocity.toArray());
	}

	@Test
	public void destroy() {
		source.destroy();
		verify(lib).alDeleteSources(1, new int[]{0});
		verify(tracker).remove(source);
	}
}
