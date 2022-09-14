package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.Frame.*;

class FrameTest {
	private Tracker frame;

	@BeforeEach
	void before() {
		frame = new Tracker();
	}

	@DisplayName("A frame that has been started...")
	@Nested
	class Started {
		@BeforeEach
		void before() {
			frame.start();
		}

		@DisplayName("can be restarted")
		@Test
		void running() {
			frame.start();
		}
	}

	@DisplayName("A frame that has ended...")
	@Nested
	class Ended {
		@BeforeEach
		void before() {
			frame.start();
		}

		@DisplayName("has a completion time")
		@Test
		void time() {
			assertNotNull(frame.time());
		}

		@DisplayName("has an elapsed duration")
		@Test
		void elapsed() {
			final Duration elapsed = frame.elapsed();
			assertNotNull(elapsed);
			assertEquals(elapsed, frame.elapsed());
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
				counter.update();
			}
			assertEquals(3, counter.fps());
		}

		@Disabled("nasty sleep")
		@DisplayName("A frame counter is reset after a second")
		@Test
		void reset() throws InterruptedException {
			counter.update();
			//Thread.sleep(1000);
			counter.update();
			assertEquals(1, counter.fps());
		}
	}
}
