package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class NodeTest {
	private Node node;

	@BeforeEach
	void before() {
		node = new Node("node");
	}

	@Nested
	class RootNodeTest {
		@Test
		void constructor() {
			assertEquals("node", node.name());
			assertEquals(null, node.parent());
			assertEquals(List.of(), node.children());
		}

		@Test
		void remove() {
			assertThrows(UnsupportedOperationException.class, () -> node.remove());
		}

		@Test
		void copy() {
			final Node copy = node.copy();
			assertEquals("node", copy.name());
			assertEquals(null, copy.parent());
			assertEquals(List.of(), copy.children());
		}

		@Test
		void equals() {
			assertEquals(node, node);
			assertEquals(node, new Node("node"));
			assertNotEquals(node, null);
			assertNotEquals(node, new Node("other"));
		}
	}

	@Nested
	class ChildNodeTest {
		private Node child;

		@BeforeEach
		void before() {
			child = new Node("child", node);
		}

		@Test
		void constructor() {
			assertEquals(node, child.parent());
			assertEquals(List.of(), child.children());
			assertEquals(List.of(child), node.children());
		}

		@Test
		void remove() {
			child.remove();
			assertEquals(null, child.parent());
			assertEquals(List.of(), node.children());
		}

		@Test
		void copy() {
			final Node copy = node.copy();
			assertEquals("node", copy.name());
			assertEquals(null, copy.parent());
			assertEquals(1, copy.children().size());

			final Node child = copy.children().getFirst();
			assertEquals("child", child.name());
			assertEquals(copy, child.parent());
		}

		@DisplayName("A copy of a leaf node is a root node")
		@Test
		void child() {
			final Node copy = child.copy();
			assertEquals("child", copy.name());
			assertEquals(null, copy.parent());
		}

		@Test
		void equals() {
			assertEquals(child, child);
			assertEquals(child, new Node("child", node));
			assertNotEquals(child, null);
			assertNotEquals(child, new Node("child"));
			assertNotEquals(child, new Node("other", node));
		}
	}

	@Nested
	class LocalTransformTest {
		@Test
		void none() {
			assertEquals(LocalTransform.NONE, node.transform());
			assertEquals(true, node.transform().isDirty());
		}

		@Test
		void transform() {
			final var transform = new LocalTransform(Matrix.translation(Axis.X));
			node.transform(transform);
			assertEquals(transform, node.transform());
			assertEquals(true, node.transform().isDirty());
		}
	}
}
