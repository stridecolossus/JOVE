package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.sarge.jove.scene.RenderLoop;
import org.sarge.jove.scene.RenderLoop.Task;

@Timeout(1000)
public class RenderLoopTest {
	private RenderLoop loop;
	private Task task;
	private CountDownLatch latch;
	private Thread thread;

	@BeforeEach
	void before() {
		latch = new CountDownLatch(1);
		task = latch::countDown;
		loop = new RenderLoop();
	}

	@AfterEach
	void after() {
		if(loop.isRunning()) {
			loop.stop();
		}

		if((thread != null) && thread.isAlive()) {
			thread.interrupt();
		}
	}

	@Test
	void constructor() {
		assertEquals(false, loop.isRunning());
	}

	@Test
	void add() {
		loop.add(task);
	}

	@Test
	void remove() {
		loop.add(task);
		loop.remove(task);
	}

	private void start() throws InterruptedException {
		loop.add(task);
		thread = new Thread(loop::run);
		thread.start();
		latch.await();
	}

	@Test
	void run() throws InterruptedException {
		start();
		assertEquals(true, loop.isRunning());
	}

	@Test
	void stop() throws InterruptedException {
		start();
		loop.stop();
		assertEquals(false, loop.isRunning());
	}

	@Test
	void stopNotRunning() {
		assertThrows(IllegalStateException.class, () -> loop.stop());
	}

	@Test
	void stopAlreadyStopped() throws InterruptedException {
		start();
		loop.stop();
		assertThrows(IllegalStateException.class, () -> loop.stop());
	}
}
