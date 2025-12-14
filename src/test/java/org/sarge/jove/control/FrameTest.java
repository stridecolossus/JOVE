package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Frame.Tracker;

class FrameTest {
	private Tracker tracker;
	private AtomicInteger listener;

	@BeforeEach
	void before() {
		listener = new AtomicInteger();
		tracker = new Tracker();
		tracker.add(_ -> listener.incrementAndGet());
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
		assertEquals(1, listener.get());
	}
}
