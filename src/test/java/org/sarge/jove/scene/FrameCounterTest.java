package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Frame;

public class FrameCounterTest {
	private FrameCounter counter;

	@BeforeEach
	void before() {
		counter = new FrameCounter();
	}

	@Test
	void frame() {
		final Frame frame = new Frame();
		frame.end(Duration.ofMillis(1));
		counter.completed(frame);
		assertEquals(1, counter.count());
		assertEquals(1, counter.fps());
	}

	@Test
	void fps() {
		for(int n = 0; n < 3; ++n) {
			final Frame frame = new Frame();
			frame.end(Duration.ofMillis(1));
			counter.completed(frame);
		}
		assertEquals(3, counter.count());
		assertEquals(1, counter.fps());
	}
}
