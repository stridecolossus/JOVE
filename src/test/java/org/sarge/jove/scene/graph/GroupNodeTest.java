package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;

class GroupNodeTest {
	private GroupNode group;

	@BeforeEach
	void before() {
		group = new GroupNode();
	}

	@DisplayName("An empty group...")
	@Nested
	class Empty {
		@DisplayName("has no children")
    	@Test
    	void constructor() {
    		assertEquals(List.of(), group.nodes().toList());
    	}

		@DisplayName("can attach a child")
    	@Test
    	void add() {
			final Node child = new Node(group);
    		assertEquals(group, child.parent());
    		assertEquals(List.of(child), group.nodes().toList());
    	}
	}

	@DisplayName("A group containing a child...")
	@Nested
	class Child {
		private Node child;

		@BeforeEach
		void before() {
			child = new Node(group);
		}

		@DisplayName("can detach the child")
		@Test
		void detach() {
			child.detach();
    		assertEquals(null, child.parent());
    		assertEquals(List.of(), group.nodes().toList());
		}

		@DisplayName("can detach all its children")
		@Test
		void clear() {
			group.clear();
    		assertEquals(null, child.parent());
    		assertEquals(List.of(), group.nodes().toList());
		}
    }
}
