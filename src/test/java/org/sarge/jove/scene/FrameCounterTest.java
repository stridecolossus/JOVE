package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Instant;

import org.junit.jupiter.api.*;

public class FrameCounterTest {
	private FrameCounter counter;

	@BeforeEach
	void before() {
		counter = new FrameCounter();
	}

	@Test
	void frame() {
		counter.frame(Instant.ofEpochMilli(0), Instant.ofEpochMilli(1));
		assertEquals(1, counter.count());
		assertEquals(0, counter.fps());
	}

	@Test
	void fps() {
		counter.frame(Instant.ofEpochMilli(0), Instant.ofEpochMilli(1));
		counter.frame(Instant.ofEpochMilli(1001), Instant.ofEpochMilli(1002));
		assertEquals(1, counter.count());
		assertEquals(2, counter.fps());
	}
}
