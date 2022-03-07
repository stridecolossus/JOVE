package org.sarge.jove.control;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(1000)
public class FrameThrottleTest {
	private FrameThrottle throttle;
	private FrameTracker tracker;
//	private Long sleep;

	@BeforeEach
	void before() {
		throttle = new FrameThrottle();
//		sleep = null;
		tracker = mock(FrameTracker.class);
	}

	@Test
	void update() {
		throttle.update(tracker);
//		assertEquals(TimeUnit.MILLISECONDS.toNanos(20), sleep);
	}

	@Test
	void updateZeroDuration() {
		when(tracker.elapsed()).thenReturn(TimeUnit.MILLISECONDS.toNanos(20));
		throttle.update(tracker);
//		assertEquals(null, sleep);
	}

	@Test
	void updatePartial() {
		when(tracker.elapsed()).thenReturn(TimeUnit.MILLISECONDS.toNanos(10));
		throttle.update(tracker);
//		assertEquals(TimeUnit.MILLISECONDS.toNanos(10), sleep);
	}

	@Test
	void throttle() {
		throttle.throttle(25);
		throttle.update(tracker);
//		assertEquals(TimeUnit.MILLISECONDS.toNanos(40), sleep);
	}
}
