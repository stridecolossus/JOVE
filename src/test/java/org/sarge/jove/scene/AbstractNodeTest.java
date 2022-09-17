package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;

public class AbstractNodeTest {
	private AbstractNode node;
	private Transform transform;

	@BeforeEach
	void before() {
		node = new AbstractNode();
		transform = Matrix4.translation(Axis.X);
	}

	@DisplayName("A new node...")
	@Nested
	class New {
		@DisplayName("does not have a parent")
		@Test
		void parent() {
			assertEquals(null, node.parent());
		}

		@DisplayName("has a default local transform")
		@Test
		void identity() {
			assertEquals(Transform.IDENTITY, node.transform());
		}

		@DisplayName("has a default world matrix")
		@Test
		void matrix() {
			assertEquals(Transform.IDENTITY, node.matrix());
		}

		@DisplayName("can have a local transform applied")
		@Test
		void transform() {
			node.transform(transform);
			assertEquals(transform, node.transform());
		}

		@DisplayName("does not have a bounding volume")
		@Test
		void volume() {
			assertEquals(Volume.EMPTY, node.volume());
		}
	}

	@DisplayName("A child node...")
	@Nested
	class Child {
		private GroupNode parent;

		@BeforeEach
		void before() {
			parent = new GroupNode();
			parent.add(node);
		}

		@DisplayName("has a parent")
		@Test
		void parent() {
			assertEquals(parent, node.parent());
		}

		@DisplayName("has a world matrix")
		@Test
		void identity() {
			assertEquals(Transform.IDENTITY, node.matrix());
		}

		@DisplayName("inherits the transform of its parent")
		@Test
		void inherits() {
			parent.transform(transform);
			assertEquals(transform, node.matrix());
		}

		@DisplayName("combines its local transform with that of its parent")
		@Test
		void multiplied() {
			parent.transform(transform);
			node.transform(transform);
			assertEquals(Matrix4.translation(Axis.X.multiply(2)), node.matrix());
		}

		@DisplayName("has its world matrix updated if the transform of any ancestor is modified")
		@Test
		void ancestor() {
			node.matrix();
			parent.transform(transform);
			assertEquals(transform, node.matrix());
		}
	}

	@Test
	void isEqual() {
		assertEquals(true, node.isEqual(node));
	}
}
