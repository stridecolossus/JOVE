package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.graph.LocalTransform.UpdateVisitor;

class LocalTransformTest {
	private LocalTransform transform;
	private Matrix matrix;
	private Node node;

	@BeforeEach
	void before() {
		node = new Node();
		transform = node.transform();
		matrix = Matrix.translation(Axis.X);
	}

	@DisplayName("A new local transform...")
	@Nested
	class New {
		@DisplayName("has an identity transform")
		@Test
		void constructor() {
			assertEquals(Matrix.IDENTITY, transform.transform());
		}

		@DisplayName("can set the local transform")
		@Test
		void set() {
			transform.set(matrix);
			assertEquals(matrix, transform.transform());
		}

		@DisplayName("can update the world matrix")
		@Test
		void update() {
			transform.update(node);
		}

		@DisplayName("has an undefined world matrix")
		@Test
		void matrix() {
			assertThrows(IllegalStateException.class, () -> transform.matrix());
		}
	}

	@DisplayName("An updated local transform...")
	@Nested
	class Updated {
		private Matrix other;

		@BeforeEach
		void before() {
			other = Matrix.translation(Axis.Y);
			transform.set(matrix);
			transform.update(node);
		}

		@DisplayName("has a world matrix")
		@Test
		void matrix() {
			assertEquals(matrix, transform.matrix());
		}

		@DisplayName("can reset the local transform of a node")
		@Test
		void update() {
			transform.set(other);
			transform.update(node);
			assertEquals(other, transform.matrix());
		}

		@DisplayName("composes the world matrix with the ancestors of the node")
		@Test
		void compose() {
			final GroupNode parent = new GroupNode();
			parent.transform().set(other);
			parent.transform().update(parent);
			node = new Node(parent);
			transform.update(node);
			assertEquals(other.multiply(matrix), transform.matrix());
		}
	}

	@Test
	void equals() {
		assertEquals(transform, transform);
		assertNotEquals(transform, null);
		assertNotEquals(transform, new LocalTransform());
	}

	@DisplayName("An update visitor...")
	@Nested
	class VisitorTests {
		private UpdateVisitor visitor;

		@BeforeEach
		void before() {
			visitor = new UpdateVisitor();
		}

		@DisplayName("updates the world matrix of a visited node")
    	@Test
    	void matrix() {
    		visitor.update(node);
			transform.matrix();
    	}

		@DisplayName("recursively visits the nodes of a scene graph")
    	@Test
    	void recurse() {
			final GroupNode parent = new GroupNode();
			node = new Node(parent);
    		visitor.update(parent);
    		parent.transform().matrix();
			node.transform().matrix();
    	}
    }
}
