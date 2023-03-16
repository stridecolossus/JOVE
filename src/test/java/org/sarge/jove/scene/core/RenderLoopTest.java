package org.sarge.jove.scene.core;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.concurrent.Future;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.FrameTimer;
import org.sarge.jove.scene.core.RenderLoop.Scheduler;

@Timeout(1)
@SuppressWarnings("rawtypes")
public class RenderLoopTest {
	private RenderLoop loop;
	private Scheduler scheduler;
	private Future future;
	private Runnable task;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		task = mock(Runnable.class);
		scheduler = mock(Scheduler.class);
		future = mock(Future.class);
		when(scheduler.start(any())).thenReturn(future);
		loop = new RenderLoop(scheduler);
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
			assertEquals(true, loop.isRunning());
		}

		@DisplayName("can be stopped")
		@Test
		void stop() {
			loop.stop();
			assertEquals(false, loop.isRunning());
			verify(future).cancel(false);
		}

		@DisplayName("cannot be started again")
		@Test
		void start() {
			assertThrows(IllegalStateException.class, () -> loop.start(task));
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

		@Disabled
		@Test
		void start() {
			loop.start(task);
			verify(listener, atLeastOnce()).update(any());
			// TODO
		}

		@Test
		void remove() {
			loop.remove(listener);
			// TODO
		}
	}

	@Test
	void continual() {
		loop = new RenderLoop(Scheduler.CONTINUAL);
		loop.start(task);
	}

	@Test
	void fixed() {
		scheduler = Scheduler.fixed(60);
		loop = new RenderLoop(scheduler);
		loop.start(task);
	}
}
