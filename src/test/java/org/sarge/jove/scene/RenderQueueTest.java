package org.sarge.jove.scene;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.scene.RenderQueue.Entry;
import org.sarge.jove.scene.RenderQueue.Order;
import org.sarge.jove.scene.RenderQueue.Renderable;

public class RenderQueueTest {
	private RenderQueue queue;
	private Renderable model;
	private Entry entry;

	@BeforeEach
	public void before() {
		queue = new RenderQueue(Order.NONE);
		model = mock(Renderable.class);
		entry = new Entry(model, queue);
	}

	@Test
	public void constructor() {
		assertEquals(Order.NONE, queue.order());
		assertNotNull(queue.queue(Point.ORIGIN));
		assertEquals(0, queue.queue(Point.ORIGIN).count());
	}

	@Nested
	class EntryTests {
		private Node node;

		@BeforeEach
		public void before() {
			node = new Node("node");
		}

		@Test
		public void entry() {
			assertEquals(model, entry.model());
			assertEquals(queue, entry.queue());
		}

		@Test
		public void equals() {
			assertEquals(true, entry.equals(entry));
			assertEquals(false, entry.equals(null));
			assertEquals(false, entry.equals(new Entry(mock(Renderable.class), queue)));
		}

		@Test
		public void add() {
			queue.add(node);
			assertArrayEquals(new Node[]{node}, queue.queue(Point.ORIGIN).toArray());
		}

		@Test
		public void remove() {
			queue.add(node);
			queue.remove(node);
			assertEquals(0, queue.queue(Point.ORIGIN).count());
		}

		@Test
		public void addNotVisible() {
			node.volume(mock(LocalVolume.class));
			queue.add(node);
			assertEquals(0, queue.queue(Point.ORIGIN).count());
		}

		@Test
		public void queues() {
			node.model(entry);
			assertEquals(Set.of(queue), RenderQueue.queues(node));
		}

		@Test
		public void queuesEmpty() {
			assertEquals(Set.of(), RenderQueue.queues(node));
		}
	}

	@Nested
	class OrderTests {
		private Node one, two;

		@BeforeEach
		public void before() {
			one = new Node("one");
			two = new Node("two");
		}

		private void build() {
			add(one, 1, new Entry(model, queue));
			add(two, 2, new Entry(model, queue));
		}

		private void add(Node node, int dist, Entry entry) {
			node.model(entry);
			node.transform(new LocalTransform(Matrix.translation(new Vector(dist, 0, 0))));
			node.transform().update(Matrix.IDENTITY);
		}

		@Test
		public void none() {
			build();
			assertArrayEquals(new Node[]{one, two}, queue.queue(Point.ORIGIN).toArray());
		}

		@Test
		public void nearest() {
			queue = new RenderQueue(Order.NEAREST);
			build();
			assertArrayEquals(new Node[]{one, two}, queue.queue(Point.ORIGIN).toArray());
		}

		@Test
		public void furthest() {
			queue = new RenderQueue(Order.FARTHEST);
			build();
			assertArrayEquals(new Node[]{two, one}, queue.queue(Point.ORIGIN).toArray());
		}
	}
}
