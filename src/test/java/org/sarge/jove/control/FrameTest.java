package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.*;

class FrameTest {
	private Frame frame;

	@BeforeEach
	void before() {
		frame = new Frame();
	}

	@DisplayName("A frame has a start time")
	@Test
	void start() {
		assertNotNull(frame.start());
		assertEquals(null, frame.elapsed());
	}

	@DisplayName("A frame can be ended")
	@Test
	void end() {
		frame.end();
		assertNotNull(frame.elapsed());
	}

	@DisplayName("A frame can be ended with a given elapsed duration")
	@Test
	void elapsed() {
		final Duration elapsed = Duration.ofMillis(1);
		frame.end(elapsed);
		assertEquals(elapsed, frame.elapsed());
	}

	@DisplayName("A frame cannot be ended more than once")
	@Test
	void ended() {
		frame.end();
		assertThrows(IllegalStateException.class, () -> frame.end());
	}
}
