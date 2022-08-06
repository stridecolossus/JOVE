package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;

public class FrameCounterTest {
	private FrameCounter counter;

	@BeforeEach
	void before() {
		counter = new FrameCounter();
	}

	@Test
	void frame() {
		counter.frame(1, 2);
		assertEquals(1, counter.count());
		assertEquals(0, counter.fps());
	}

	@Test
	void fps() {
		// TODO
	}
}
