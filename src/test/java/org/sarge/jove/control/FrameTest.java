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

	@DisplayName("A new unstarted frame...")
	@Nested
	class New {
		@DisplayName("can be started")
		@Test
		void start() {
			frame.start();
		}

		@DisplayName("cannot be stopped")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> frame.stop());
		}

		@DisplayName("has an undefined elapsed duration")
		@Test
		void elapsed() {
			assertEquals(Duration.ZERO, frame.elapsed());
		}
	}

	@DisplayName("A running frame...")
	@Nested
	class Started {
		@BeforeEach
		void before() {
			frame.start();
		}

		@DisplayName("cannot be restarted")
		@Test
		void start() {
			assertThrows(IllegalStateException.class, () -> frame.start());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			frame.stop();
		}

		@DisplayName("has an undefined elapsed duration")
		@Test
		void elapsed() {
			assertThrows(IllegalStateException.class, () -> frame.elapsed());
			assertThrows(IllegalStateException.class, () -> frame.time());
		}
	}

	@DisplayName("A completed frame...")
	@Nested
	class Ended {
		@BeforeEach
		void before() {
			frame.start();
			frame.stop();
		}

		@DisplayName("can be restarted")
		@Test
		void start() {
			frame.start();
		}

		@DisplayName("cannot be stopped again until it has been restarted")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> frame.stop());
		}

		@DisplayName("has an elapsed duration")
		@Test
		void elapsed() {
			final Duration elapsed = frame.elapsed();
			assertEquals(false, elapsed.isNegative());
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
				counter.update(frame);
			}
			assertEquals(3, counter.fps());
		}

		@Disabled("nasty sleep")
		@DisplayName("A frame counter is reset after a second")
		@Test
		void reset() throws InterruptedException {
			counter.update(frame);
			//Thread.sleep(1000);
			counter.update(frame);
			assertEquals(1, counter.fps());
		}
	}
}
