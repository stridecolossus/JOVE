package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Frame.Counter;

class FrameTest {
	private Frame frame;

	@BeforeEach
	void before() {
		frame = new Frame();
	}

	@DisplayName("A frame that has been started...")
	@Nested
	class Started {
		@BeforeEach
		void before() {
			frame.start();
		}

		@DisplayName("has a start time")
		@Test
		void time() {
			assertNotNull(frame.time());
		}

		@DisplayName("can be ended")
		@Test
		void end() {
			assertNotNull(frame.end());
		}

		@DisplayName("cannot be started again")
		@Test
		void running() {
			assertThrows(IllegalStateException.class, () -> frame.start());
		}
	}

	@DisplayName("A frame that has ended...")
	@Nested
	class Ended {
		private Duration elapsed;

		@BeforeEach
		void before() {
			elapsed = Duration.ofSeconds(1);
			frame.start();
			frame.end(elapsed);
		}

		@DisplayName("has an elapsed duration")
		@Test
		void elapsed() {
			assertNotNull(frame.time());
			assertEquals(elapsed, frame.elapsed());
		}

		@DisplayName("can be restarted")
		@Test
		void restart() {
			frame.start();
		}

		@DisplayName("cannot be ended again")
		@Test
		void end() {
			assertThrows(IllegalStateException.class, () -> frame.end());
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
				counter.frame();
			}
			assertEquals(3, counter.fps());
		}

		@DisplayName("A frame counter is reset after a second")
		@Test
		void reset() throws InterruptedException {
			counter.frame();
			Thread.sleep(1000);
			counter.frame();
			assertEquals(1, counter.fps());
		}
	}
}
