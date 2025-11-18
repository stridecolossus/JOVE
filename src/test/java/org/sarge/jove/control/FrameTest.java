package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Frame.*;

class FrameTest {
	private static class MockFrameListener implements Frame.Listener {
		private int count;

		@Override
		public void update(Frame frame) {
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
		final Timer timer = tracker.begin();
		timer.end();
	}

	@Test
	void already() {
		final Timer timer = tracker.begin();
		timer.end();
		assertThrows(IllegalStateException.class, () -> timer.end());
	}

	@Test
	void listener() {
		tracker.add(listener);
		for(int n = 0; n < 3; ++n) {
    		final Timer timer = tracker.begin();
    		timer.end();
		}
		assertEquals(3, listener.count);
	}

	@Test
	void parallel() {
		tracker.add(listener);
		final Timer one = tracker.begin();
		final Timer two = tracker.begin();
		two.end();
		one.end();
		assertEquals(2, listener.count);
	}
}
