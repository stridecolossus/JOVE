package org.sarge.jove.scene.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.FrameTimer;
import org.sarge.jove.scene.core.RenderLoop.Scheduler;

@Timeout(1)
public class RenderLoopTest {
	private CountDownLatch latch;
	private RenderLoop loop;

	@BeforeEach
	void before() {
		latch = new CountDownLatch(1);
		loop = new RenderLoop(Scheduler.CONTINUAL);
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
			loop.start(mock(Runnable.class));
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
			loop.start(latch::countDown);
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
			assertThrows(IllegalStateException.class, () -> loop.start(mock(Runnable.class)));
		}
	}

	@Nested
	class ListenerTests {
		private FrameTimer.Listener listener;

		@BeforeEach
		void before() {
			listener = mock(FrameTimer.Listener.class);
			loop.add(listener);
		}

		@Test
		void start() throws InterruptedException {
			loop.start(latch::countDown);
			latch.await();
			verify(listener).update(any());
		}
	}

	@Test
	void fixed() {
		loop = new RenderLoop(Scheduler.fixed(60));
		loop.start(mock(Runnable.class));
	}
}
