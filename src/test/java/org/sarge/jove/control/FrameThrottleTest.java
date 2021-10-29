package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(1000)
public class FrameThrottleTest {
	private FrameThrottle throttle;
	private FrameTracker tracker;
	private Long sleep;

	@BeforeEach
	void before() {
		throttle = new FrameThrottle() {
			@Override
			protected void sleep(long duration) {
				sleep = duration;
			}
		};
		sleep = null;
		tracker = mock(FrameTracker.class);
	}

	@Test
	void update() {
		when(tracker.elapsed()).thenReturn(25L);
		throttle.update(tracker);
		assertEquals(25L, sleep);
	}

	@Test
	void updateZeroDuration() {
		when(tracker.elapsed()).thenReturn(50L);
		throttle.update(tracker);
		assertEquals(null, sleep);
	}

	@Test
	void duration() {
		when(tracker.elapsed()).thenReturn(50L);
		throttle.duration(100);
		throttle.update(tracker);
		assertEquals(100 - 50, sleep);
	}

	@Test
	void sleep() {
		throttle = new FrameThrottle();
		when(tracker.elapsed()).thenReturn(25L);
		throttle.update(tracker);
	}
}
