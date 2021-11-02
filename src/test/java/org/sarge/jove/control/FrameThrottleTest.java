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
		throttle.update(tracker);
		assertEquals(20, sleep);
	}

	@Test
	void updateZeroDuration() {
		when(tracker.elapsed()).thenReturn(20L);
		throttle.update(tracker);
		assertEquals(null, sleep);
	}

	@Test
	void updatePartial() {
		when(tracker.elapsed()).thenReturn(10L);
		throttle.update(tracker);
		assertEquals(10, sleep);
	}

	@Test
	void throttle() {
		throttle.throttle(25);
		throttle.update(tracker);
		assertEquals(40, sleep);
	}
}
