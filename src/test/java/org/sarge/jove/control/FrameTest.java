package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Frame.Tracker;

class FrameTest {
	private static class MockFrameListener implements Frame.Listener {
		private int count;

		@Override
		public void end(Frame frame) {
			assertNotNull(frame);
			++count;
		}
	}

	private Tracker tracker;
	private MockFrameListener listener;

	@BeforeEach
	void before() {
		listener = new MockFrameListener();
		tracker = new Tracker();
	}

	@Test
	void elapsed() {
		final Frame frame = new Frame(Instant.ofEpochSecond(1), Instant.ofEpochSecond(3));
		assertEquals(Duration.ofSeconds(2), frame.elapsed());
	}

	@Test
	void begin() {
		tracker.begin();
	}

	@Test
	void end() {
		final var timer = tracker.begin();
		timer.run();
	}

	@Test
	void already() {
		final var timer = tracker.begin();
		timer.run();
		assertThrows(IllegalStateException.class, () -> timer.run());
	}

	@Test
	void listener() {
		tracker.add(listener);
		for(int n = 0; n < 3; ++n) {
    		final var timer = tracker.begin();
    		timer.run();
		}
		assertEquals(3, listener.count);
	}

	@Test
	void parallel() {
		tracker.add(listener);
		final var one = tracker.begin();
		final var two = tracker.begin();
		two.run();
		one.run();
		assertEquals(2, listener.count);
	}
}
