package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Frame.Listener;

public class FrameTest {
	private Frame frame;

	@BeforeEach
	public void before() {
		frame = new Frame();
	}

	@Test
	public void constructor() {
		assertTrue(frame.now() > 0);
		assertEquals(0, frame.elapsed());
		assertEquals(0, frame.rate());
	}

	@Test
	public void update() throws InterruptedException {
		final long end = System.currentTimeMillis() + 1000;
		int count = 0;
		while(true) {
			Thread.sleep(100);
			frame.update();
			assertTrue(frame.elapsed() > 0);
			++count;
			if(System.currentTimeMillis() > end) break;
		}
		assertTrue(count > 0);
		assertEquals(count, frame.rate());
	}

	@Test
	public void listener() {
		final Listener listener = mock(Listener.class);
		frame.add(listener);
		frame.update();
		verify(listener).update(frame);
	}
}
