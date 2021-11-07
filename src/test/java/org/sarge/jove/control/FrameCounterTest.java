package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class FrameCounterTest {
	private static final long FPS = 50;

	private FrameCounter counter;
	private FrameTracker tracker;

	@BeforeEach
	void before() {
		counter = new FrameCounter();
		tracker = mock(FrameTracker.class);
		when(tracker.elapsed()).thenReturn(TimeUnit.SECONDS.toNanos(1) / FPS);
	}

	@Test
	void constructor() {
		assertEquals(0, counter.count());
	}

	@Test
	void update() {
		counter.update(tracker);
		assertEquals(0, counter.count());
	}

	@Test
	void reset() {
		for(int n = 0; n < FPS; ++n) {
			counter.update(tracker);
		}
		assertEquals(FPS, counter.count());
	}

	@Test
	void multiple() {
		reset();
		reset();
		assertEquals(FPS, counter.count());
	}
}
