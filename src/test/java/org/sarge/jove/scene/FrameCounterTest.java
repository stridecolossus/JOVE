package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.scene.FrameCounter.Listener;

public class FrameCounterTest {
	private FrameCounter counter;
	private Runnable task;

	@BeforeEach
	void before() {
		task = mock(Runnable.class);
		counter = new FrameCounter(task);
	}

	@Test
	void run() {
		counter.run();
		assertEquals(1, counter.count());
		assertEquals(0, counter.fps());
	}

	@Test
	void fps() throws InterruptedException {
		counter.run();
		Thread.sleep(1000);
		counter.run();
		assertEquals(1, counter.count());
		assertEquals(1, counter.fps());
	}

	@Test
	void listener() {
		final Listener listener = mock(Listener.class);
		counter.add(listener);
		counter.run();
		verify(listener).frame(anyLong(), anyLong());
		// TODO - arg should the counter
	}
}
