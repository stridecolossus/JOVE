package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.MutableRotation;
import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.graph.LocalTransform.UpdateVisitor;

class LocalTransformTest {
	private LocalTransform transform;
	private UpdateVisitor visitor;

	@BeforeEach
	void before() {
		transform = new LocalTransform(Matrix.translation(Axis.X));
		visitor = new UpdateVisitor();
	}

	@Test
	void isDirty() {
		assertEquals(true, transform.isDirty());
		assertEquals(null, transform.world());
	}

	@Test
	void update() {
		final Node node = new Node("whatever");
		node.transform(transform);
		visitor.update(node);
		assertEquals(false, transform.isDirty());
		assertEquals(Matrix.translation(Axis.X), transform.world());
	}

	@Test
	void none() {
		assertEquals(false, LocalTransform.NONE.isDirty());
		assertEquals(Matrix.IDENTITY, LocalTransform.NONE.world());
	}

	@Test
	void mutable() {
		final var mutable = LocalTransform.mutable(new MutableRotation(new AxisAngle(Axis.X, 0)));
		final Node node = new Node("whatever");
		node.transform(mutable);
		visitor.update(node);
		assertEquals(true, mutable.isDirty());
	}

	@Nested
	class Inherited {
		private Node parent, child;

		@BeforeEach
		void before() {
			parent = new Node("parent");
			child = new Node("child", parent);
			parent.transform(new LocalTransform(Matrix.translation(Axis.Y)));
			child.transform(transform);
		}

		@Test
		void transform() {
			assertEquals(transform, child.transform());
		}

		@Test
		void isDirty() {
			assertEquals(true, transform.isDirty());
			assertEquals(null, transform.world());
		}

		@Test
		void update() {
			final Matrix expected = Matrix.translation(Axis.Y).multiply(Matrix.translation(Axis.X));
			visitor.update(child);
			assertEquals(false, transform.isDirty());
			assertEquals(expected, transform.world());
		}

		@Test
		void propagate() {
			visitor.update(child);
			assertEquals(false, parent.transform().isDirty());
			assertEquals(Matrix.translation(Axis.Y), parent.transform().world());
		}
	}
}
