package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.spy;
import static org.sarge.jove.control.Playable.State.*;

import org.junit.jupiter.api.*;

public class AbstractPlayableTest {
	private AbstractPlayable playable;

	@BeforeEach
	void before() {
		playable = spy(AbstractPlayable.class);
	}

	@Test
	void constructor() {
		assertEquals(false, playable.isPlaying());
	}

	@DisplayName("A playable can be played")
	@Test
	void play() {
		playable.state(PLAY);
		assertEquals(true, playable.isPlaying());
	}

	@DisplayName("A playable can be paused")
	@Test
	void pause() {
		playable.state(PLAY);
		playable.state(PAUSE);
		assertEquals(false, playable.isPlaying());
	}

	@DisplayName("A paused playable can be restarted")
	@Test
	void unpause() {
		playable.state(PLAY);
		playable.state(PAUSE);
		playable.state(PLAY);
		assertEquals(true, playable.isPlaying());
	}

	@DisplayName("A playable can be stopped")
	@Test
	void stop() {
		playable.state(PLAY);
		playable.state(STOP);
		assertEquals(false, playable.isPlaying());
	}
}
