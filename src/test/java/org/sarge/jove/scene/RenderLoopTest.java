package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;

@Timeout(1)
public class RenderLoopTest {
	private RenderLoop loop;
	private Runnable task;

	@BeforeEach
	void before() {
		task = mock(Runnable.class);
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

		@DisplayName("has a default FPS configured")
		@Test
		void rate() {
			assertEquals(1000 / 60, loop.rate());
		}

		@DisplayName("can configure the target FPS")
		@Test
		void fps() {
			loop.rate(10);
			assertEquals(1000 / 10, loop.rate());
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
			assertEquals(true, loop.isRunning());
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

		@DisplayName("cannot change the target FPS")
		@Test
		void fps() {
			assertThrows(IllegalStateException.class, () -> loop.rate(60));
		}
	}
}
