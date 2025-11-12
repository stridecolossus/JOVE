package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;

import org.junit.jupiter.api.*;

class FrameCounterTest {
	private FrameCounter counter;

	@BeforeEach
	void before() {
		counter = new FrameCounter();
	}

	@Test
	void empty() {
		assertEquals(0, counter.count());
	}

	@Test
	void start() {
		counter.start();
	}

	@Test
	void startAlreadyStarted() {
		counter.start();
		assertThrows(IllegalStateException.class, () -> counter.start());
	}

	@Test
	void stop() {
		counter.start();
		counter.stop();
		assertEquals(1, counter.count());
	}

	@Test
	void stopNotStarted() {
		assertThrows(IllegalStateException.class, () -> counter.stop());
	}

	@Test
	void elapsed() throws InterruptedException {
		counter.start();
		Thread.sleep(50);
		final Duration elapsed = counter.stop();
		assertEquals(true, elapsed.isPositive());
	}

	@Test
	void window() throws InterruptedException {
		counter.start();
		counter.stop();
		Thread.sleep(1000);
		counter.start();
		counter.stop();
		assertEquals(1, counter.count());
	}
}
