package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;

class NodeTest {
	@DisplayName("A new node...")
	@Nested
	class New {
		private Node node;

		@BeforeEach
		void before() {
			node = new Node();
		}

		@DisplayName("has a local transform")
    	@Test
    	void transform() {
    		assertNotNull(node.transform());
    	}

		@Test
		void equals() {
			assertEquals(node, node);
			assertNotEquals(node, null);
			assertNotEquals(node, new Node());
		}
	}

	@DisplayName("A root node...")
	@Nested
	class Root {
		private Node root;

		@BeforeEach
		void before() {
			root = new Node();
		}

		@DisplayName("does not have a parent")
    	@Test
    	void parent() {
    		assertEquals(null, root.parent());
    	}

//		@DisplayName("is the root node of the scene graph")
//    	@Test
//    	void root() {
//    		assertEquals(root, root.root());
//    	}

		@DisplayName("cannot be detached")
		@Test
		void detach() {
			assertThrows(IllegalStateException.class, () -> root.detach());
		}
	}

	@DisplayName("A child node...")
	@Nested
	class Child {
		private Node node;
		private RootNode parent;

		@BeforeEach
		void before() {
			parent = new RootNode();
			node = new Node(parent);
		}

		@DisplayName("has a parent")
		@Test
		void parent() {
    		assertEquals(parent, node.parent());
		}

		@DisplayName("has a root node")
    	@Test
    	void root() {
    		assertEquals(parent, node.root());
    	}

		@DisplayName("can be detached from its parent")
		@Test
		void detach() {
			node.detach();
    		assertEquals(null, node.parent());
    		assertEquals(List.of(), parent.nodes().toList());
		}
	}
}
