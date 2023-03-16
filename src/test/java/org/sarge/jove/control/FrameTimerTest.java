package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.FrameTimer.Counter;

class FrameTimerTest {
	private FrameTimer frame;

	@BeforeEach
	void before() {
		frame = new FrameTimer();
	}

	@DisplayName("A new frame timer...")
	@Nested
	class Started {
		@DisplayName("can be completed")
		@Test
		void stop() {
			frame.stop();
		}

		@DisplayName("does not have a completion time")
		@Test
		void time() {
			assertThrows(IllegalStateException.class, () -> frame.time());
		}
	}

	@DisplayName("A completed frame...")
	@Nested
	class Completed {
		@BeforeEach
		void before() {
			frame.stop();
		}

		@DisplayName("records the elapsed duration of the frame")
		@Test
		void time() {
			assertNotNull(frame.elapsed());
			assertNotNull(frame.time());
		}

		@DisplayName("cannot be stopped again")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> frame.stop());
		}
	}

	@Nested
	class CounterTests {
		private Counter counter;

		@BeforeEach
		void before() {
			counter = new Counter();
		}

		@DisplayName("A frame counter records the number of frame updates over a second")
		@Test
		void fps() {
			for(int n = 0; n < 3; ++n) {
				final var frame = new FrameTimer();
				frame.stop();
				counter.update(frame);
			}
			assertEquals(3, counter.fps());
		}

		@DisplayName("A frame counter is reset after a second")
		@Test
		void reset() throws InterruptedException {
			frame = mock(FrameTimer.class);
			when(frame.time()).thenReturn(Instant.now());
			when(frame.elapsed()).thenReturn(Duration.ofSeconds(1));
			counter.update(frame);
			assertEquals(1, counter.fps());
		}
	}
}
