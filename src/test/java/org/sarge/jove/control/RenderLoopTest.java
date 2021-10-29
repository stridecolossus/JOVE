package org.sarge.jove.control;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.sarge.jove.control.RenderLoop.Task;

@Timeout(1000)
public class RenderLoopTest {
	private RenderLoop app;
	private Task task;
	private CountDownLatch latch;
	private Thread thread;

	@BeforeEach
	void before() {
		latch = new CountDownLatch(1);
		task = latch::countDown;
		app = new RenderLoop(List.of(task));
	}

	@AfterEach
	void after() {
		if(app.isRunning()) {
			app.stop();
		}

		if((thread != null) && thread.isAlive()) {
			thread.interrupt();
		}
	}

	@Test
	void constructor() {
		assertEquals(false, app.isRunning());
	}

	private void start() throws InterruptedException {
		thread = new Thread(app::run);
		thread.start();
		latch.await();
	}

	@Test
	void run() throws InterruptedException {
		start();
		assertEquals(true, app.isRunning());
	}

	@Test
	void stop() throws InterruptedException {
		start();
		app.stop();
		assertEquals(false, app.isRunning());
	}

	@Test
	void stopNotRunning() {
		assertThrows(IllegalStateException.class, () -> app.stop());
	}

	@Test
	void stopAlreadyStopped() throws InterruptedException {
		start();
		app.stop();
		assertThrows(IllegalStateException.class, () -> app.stop());
	}
}
