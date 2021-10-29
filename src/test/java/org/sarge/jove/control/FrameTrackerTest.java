package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.control.FrameTracker.Listener;

public class FrameTrackerTest {
	private FrameTracker tracker;
	private Listener listener;

	@BeforeEach
	void before() {
		tracker = new FrameTracker();
		listener = mock(Listener.class);
	}

	@Test
	void constructor() {
		assertEquals(0, tracker.elapsed());
	}

	@Test
	void add() {
		tracker.add(listener);
		tracker.execute();
		verify(listener).update(tracker);
	}

	@Test
	void remove() {
		tracker.add(listener);
		tracker.remove(listener);
		tracker.execute();
		verifyNoMoreInteractions(listener);
	}

	@Test
	void clear() {
		tracker.add(listener);
		tracker.clear();
		tracker.execute();
		verifyNoMoreInteractions(listener);
	}

	@Test
	void execute() throws InterruptedException {
		final long start = tracker.time();
		Thread.sleep(50);
		tracker.execute();
		assertTrue(tracker.time() > start);
		assertTrue(tracker.elapsed() > 0);
	}
}
