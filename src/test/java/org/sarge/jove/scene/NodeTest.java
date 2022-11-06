package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.Node.Visitor;

class NodeTest {
	private Node node;

	@BeforeEach
	void before() {
		node = new Node();
	}

	@DisplayName("A new node...")
	@Nested
	class New {
		@DisplayName("is a root node")
		@Test
		void isRoot() {
			assertEquals(true, node.isRoot());
			assertEquals(null, node.parent());
		}

		@DisplayName("has a local transform")
		@Test
		void transform() {
			final LocalTransform transform = node.transform();
			assertEquals(true, transform.isDirty());
			assertEquals(Matrix4.IDENTITY, transform.transform());
		}

		@DisplayName("has a local material property")
		@Test
		void material() {
			final LocalMaterial mat = node.material();
			assertEquals(true, mat.isDirty());
		}

		@DisplayName("has a local bounding volume property")
		@Test
		void volume() {
			assertEquals(EmptyVolume.INSTANCE, node.volume());
		}

		@DisplayName("cannot be detached")
		@Test
		void detach() {
			assertThrows(IllegalStateException.class, () -> node.detach());
		}

		// TODO - reset
	}

	@Nested
	class Operations {
		@DisplayName("can be visited")
		@Test
		void visit() {
			final Visitor visitor = mock(Visitor.class);
			node.accept(visitor);
			verify(visitor).visit(node);
		}

		@DisplayName("can override the bounding volume of that node")
		@Test
		void set() {
			final Volume vol = mock(Volume.class);
			node.set(vol);
			assertEquals(vol, node.volume());
		}
	}

	@DisplayName("A child node...")
	@Nested
	class Child {
		private Node parent;

		@BeforeEach
		void before() {
			parent = new Node();
			node.attach(parent);
		}

		@DisplayName("has a parent node")
		@Test
		void parent() {
			assertEquals(parent, node.parent());
			assertEquals(false, node.isRoot());
		}

		@DisplayName("cannot be attached to another node")
		@Test
		void attach() {
			assertThrows(IllegalStateException.class, () -> node.attach(new Node()));
		}

		@DisplayName("can be detached from the scene")
		@Test
		void detach() {
			node.detach();
			assertEquals(null, node.parent());
			assertEquals(true, node.isRoot());
		}
	}

	@Nested
	class CopyTests {
		@Test
		void constructor() {
			final Node copy = new Node(node);
			assertEquals(Matrix4.IDENTITY, copy.transform().transform());
			assertEquals(true, copy.transform().isDirty());
			assertEquals(true, copy.material().isDirty());
			assertEquals(EmptyVolume.INSTANCE, copy.volume());
			assertEquals(true, copy.isRoot());
		}

		@Test
		void copy() {
			assertThrows(UnsupportedOperationException.class, () -> node.copy());
		}
	}

	@Test
	void equals() {
		assertEquals(node, node);
		assertNotEquals(node, null);
		assertNotEquals(node, new Node(node));
	}
}
