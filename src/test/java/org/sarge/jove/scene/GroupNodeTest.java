package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.model.Model;

public class GroupNodeTest {
	private GroupNode group;
	private ModelNode node;

	@BeforeEach
	void before() {
		group = new GroupNode();
		node = new ModelNode(mock(Model.class));
	}

	@DisplayName("A new empty group node...")
	@Nested
	class Empty {
		@DisplayName("has no children")
		@Test
		void empty() {
			assertEquals(0, group.nodes().count());
		}

		@DisplayName("can add a child node")
		@Test
		void add() {
			group.add(node);
			assertArrayEquals(new Node[]{node}, group.nodes().toArray());
			assertEquals(group, node.parent());
		}

		@DisplayName("cannot add itself")
		@Test
		void self() {
			assertThrows(IllegalArgumentException.class, () -> group.add(group));
		}

		@DisplayName("cannot remove a child that has not been attached")
		@Test
		void remove() {
			assertThrows(IllegalStateException.class, () -> group.remove(node));
		}
	}

	@DisplayName("A group containing child nodes...")
	@Nested
	class Children {
		private GroupNode child;

		@BeforeEach
		void before() {
			child = new GroupNode();
			group.add(child);
			group.add(node);
		}

		@DisplayName("can enumerate its children")
		@Test
		void nodes() {
			assertArrayEquals(new Node[]{child, node}, group.nodes().toArray());
		}

		@DisplayName("cannot add a node that is already attached")
		@Test
		void added() {
			assertThrows(AssertionError.class, () -> group.add(node));
			assertThrows(AssertionError.class, () -> group.add(child));
		}

		@DisplayName("can remove attached nodes")
		@Test
		void remove() {
			group.remove(node);
			assertEquals(false, group.nodes().toList().contains(node));
			assertEquals(null, node.parent());
		}

		@DisplayName("can remove all attached nodes")
		@Test
		void clear() {
			group.clear();
			assertEquals(0, group.nodes().count());
			assertEquals(null, node.parent());
		}
	}

	@Test
	void equals() {
		assertEquals(group, group);
		assertNotEquals(group, null);
		assertNotEquals(group, new GroupNode());
	}
}
