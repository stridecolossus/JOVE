package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.RenderLoop;

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

		@DisplayName("cannot be paused")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> loop.pause());
			assertThrows(IllegalStateException.class, () -> loop.restart());
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

		@DisplayName("can be paused")
		@Test
		void pause() {
			loop.pause();
			assertEquals(true, loop.isRunning());
		}

		@DisplayName("cannot be unpaused")
		@Test
		void unpause() {
			assertThrows(IllegalStateException.class, () -> loop.restart());
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

	@DisplayName("A paused render loop...")
	@Nested
	class Paused {
		@BeforeEach
		void before() {
			loop.start(task);
			loop.pause();
		}

		@DisplayName("is still active")
		void paused() {
			assertEquals(true, loop.isRunning());
		}

		@DisplayName("cannot be paused again")
		@Test
		void pause() {
			assertThrows(IllegalStateException.class, () -> loop.pause());
		}

		@DisplayName("can be restarted")
		@Test
		void stop() {
			loop.restart();
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

//
//	@DisplayName("A frame listener...")
//	@Nested
//	class ListenerTests {
//		private Frame.Listener listener;
//
//		@BeforeEach
//		void before() {
//			listener = mock(Frame.Listener.class);
//			loop.add(listener);
//		}
//
//		@DisplayName("can be attached to the loop to receive frame completion events")
//		@Test
//		void add() throws InterruptedException {
//			loop.start(task);
//			latch.await();
//			verify(listener, atLeastOnce()).update(any(Frame.class));
//		}
//
//		@DisplayName("can be removed from the loop")
//		@Test
//		void remove() throws InterruptedException {
//			loop.remove(listener);
//			loop.start(task);
//			latch.await();
//			verifyNoInteractions(listener);
//		}
//	}
//
//	@DisplayName("Exceptions caused by a render task can be delegated to a handler")
//	@SuppressWarnings("unchecked")
//	@Test
//	void handler() throws InterruptedException {
//		final Consumer<Exception> handler = mock(Consumer.class);
//		final var e = new RuntimeException();
//		task = () -> {
//			latch.countDown();
//			throw e;
//		};
//		loop.handler(handler);
//		loop.start(task);
//		latch.await();
//		verify(handler, atLeastOnce()).accept(e);
//		assertEquals(true, loop.isRunning());
//	}
//}
