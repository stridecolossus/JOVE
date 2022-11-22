package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.scene.graph.*;
import org.sarge.jove.scene.graph.Node.Visitor;

public class GroupNodeTest {
	private GroupNode group;
	private Node node;

	@BeforeEach
	void before() {
		group = new GroupNode();

		node = new Node() {
			@Override
			public Node copy() {
				return new Node();
			}
		};
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
		@BeforeEach
		void before() {
			group.add(node);
		}

		@DisplayName("can enumerate its children")
		@Test
		void nodes() {
			assertArrayEquals(new Node[]{node}, group.nodes().toArray());
		}

		@DisplayName("is visited recursively")
		@Test
		void visitor() {
			final Visitor visitor = mock(Visitor.class);
			group.accept(visitor);
			verify(visitor).visit(group);
			verify(visitor).visit(node);
		}

		@DisplayName("cannot add a node that is already attached")
		@Test
		void added() {
			assertThrows(IllegalStateException.class, () -> group.add(node));
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

		@DisplayName("creates a new sub-tree when it is copied")
		@Test
		void copy() {
//			group.material().set(mock(Material.class)); // TODO
			final GroupNode copy = group.copy();
			assertEquals(1, copy.nodes().count());
		}
	}

	@Test
	void equals() {
		assertEquals(group, group);
		assertNotEquals(group, null);
		assertNotEquals(group, new GroupNode());
	}
}
