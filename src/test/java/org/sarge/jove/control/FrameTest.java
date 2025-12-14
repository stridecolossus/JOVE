package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Frame.*;

class FrameTest {
	private static class MockFrameListener implements Listener {
		private int count;

		@Override
		public void frame(Frame frame) {
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
	void timer() throws Exception {
		try(final var _ = tracker.timer()) {
			// Empty
		}
	}

	@Test
	void listener() throws Exception {
		tracker.add(listener);
		for(int n = 0; n < 3; ++n) {
			try(final var _ = tracker.timer()) {
				// Empty
			}
		}
		assertEquals(3, listener.count);
	}

//	@Test
//	void parallel() {
//		tracker.add(listener);
//		final var one = tracker.timer();
//		final var two = tracker.timer();
//		two.run();
//		one.run();
//		assertEquals(2, listener.count);
//	}

	// TODO
	@Test
	void periodic() {
		final var periodic = Listener.periodic(Duration.ofSeconds(1), listener);
		final Instant start = Instant.now();
		periodic.frame(new Frame(start, start.plusMillis(500)));
		assertEquals(0, listener.count);
		periodic.frame(new Frame(start, start.plusMillis(1500)));
		assertEquals(1, listener.count);
	}
}
