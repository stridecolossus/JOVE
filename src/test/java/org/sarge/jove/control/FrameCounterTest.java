package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.*;

class FrameCounterTest {
	private FrameCounter counter;

	@BeforeEach
	void before() {
		counter = new FrameCounter();
	}

	@Test
	void empty() {
		assertEquals(0, counter.count());
	}

	@Test
	void update() {
		final Instant start = Instant.ofEpochMilli(0);
		counter.end(new Frame(start, start.plusMillis(50)));
		assertEquals(1, counter.count());
	}

	@Test
	void cull() {
		// Record frame that will be culled
		final Instant one = Instant.ofEpochMilli(0);
		counter.end(new Frame(one, one.plusMillis(50)));

		// Record new frame
		final Instant two = Instant.ofEpochMilli(1000);
		counter.end(new Frame(two, two.plusMillis(50)));

		// Check old frame is culled
		assertEquals(1, counter.count());
	}

	@Test
	void count() {
		final long elapsed = 1000 / 60;
		for(int n = 0; n < 60; ++n) {
			final Instant start = Instant.ofEpochMilli(n * elapsed);
			counter.end(new Frame(start, start.plusMillis(elapsed)));
		}
		assertEquals(60, counter.count());
	}
}
