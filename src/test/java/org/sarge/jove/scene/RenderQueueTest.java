package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class RenderQueueTest {
	private RenderQueue queue;

	@BeforeEach
	void before() {
		queue = new RenderQueue(1, false);
	}

	@Test
	void constructor() {
		assertEquals(1, queue.order());
		assertEquals(false, queue.reverse());
	}

	@Test
	void compare() {
		assertEquals(0, queue.compareTo(queue));
		assertEquals(1, queue.compareTo(RenderQueue.OPAQUE));
	}

	@Test
	void equals() {
		assertEquals(queue, queue);
		assertEquals(queue, new RenderQueue(1, false));
		assertNotEquals(queue, null);
		assertNotEquals(queue, RenderQueue.TRANSLUCENT);
	}
}
