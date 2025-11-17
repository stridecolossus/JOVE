package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.*;

@Timeout(1)
public class RenderLoopTest {
	private CountDownLatch latch;
	private RenderLoop loop;
	private Runnable task;

	@BeforeEach
	void before() {
		latch = new CountDownLatch(2);
		task = latch::countDown;
		loop = new RenderLoop();
	}

	@AfterEach
	void after() {
		if(loop.isRunning()) {
			loop.stop();
		}
	}

	@DisplayName("A new render loop...")
	@Nested
	class New {
		@DisplayName("is not initially running")
		@Test
		void isRunning() {
			assertEquals(false, loop.isRunning());
		}

		@DisplayName("can be started")
		@Test
		void start() {
			loop.start(task);
			assertEquals(true, loop.isRunning());
		}

		@DisplayName("cannot be stopped")
		@Test
		void stop() {
			assertThrows(IllegalStateException.class, () -> loop.stop());
		}
	}

	@DisplayName("A running render loop...")
	@Nested
	class Running {
		@BeforeEach
		void before() {
			loop.start(task);
		}

		@Test
		void started() throws InterruptedException {
			latch.await();
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			loop.stop();
			assertEquals(false, loop.isRunning());
		}

		@DisplayName("cannot be started again")
		@Test
		void start() {
			assertThrows(IllegalStateException.class, () -> loop.start(task));
		}
	}

	@DisplayName("The frame rate of the loop...")
	@Nested
	class FrameRateTests {
    	@DisplayName("has a default FPS target")
    	@Test
    	void rate() {
    		assertEquals(60, loop.rate());
    	}

    	@DisplayName("can be set to an FPS target")
    	@Test
    	void modify() {
    		loop.rate(50);
    		assertEquals(50, loop.rate());
    	}

    	@DisplayName("cannot be set if the loop is running")
    	@Test
    	void running() {
    		loop.start(task);
    		assertThrows(IllegalStateException.class, () -> loop.rate(50));
    	}

    	@DisplayName("cannot be zero")
    	@Test
    	void invalid() {
    		assertThrows(IllegalArgumentException.class, () -> loop.rate(0));
    		assertThrows(IllegalArgumentException.class, () -> loop.rate(-1));
    	}
	}
}
