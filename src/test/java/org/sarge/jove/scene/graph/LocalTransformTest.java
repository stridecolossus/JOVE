package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.control.MutableRotation;
import org.sarge.jove.geometry.*;

class LocalTransformTest {
	private LocalTransform transform;

	@BeforeEach
	void before() {
		transform = new LocalTransform(Matrix.translation(Axis.X));
	}

	@Nested
	class Dirty {
		@Test
		void dirty() {
			assertEquals(true, transform.isDirty());
			assertEquals(null, transform.world());
		}

		@Test
		void update() {
			transform.update(new Node("whatever"));
			assertEquals(false, transform.isDirty());
		}

		@Test
		void clear() {
			transform.clear();
			assertEquals(true, transform.isDirty());
			assertEquals(null, transform.world());
		}
	}

	@Nested
	class Updated {
		@BeforeEach
		void before() {
			transform.update(new Node("whatever"));
		}

		@Test
		void dirty() {
			assertEquals(false, transform.isDirty());
			assertEquals(Matrix.translation(Axis.X), transform.world());
		}

		@Test
		void clear() {
			transform.clear();
			assertEquals(true, transform.isDirty());
			assertEquals(null, transform.world());
		}
	}

	@Test
	void inherited() {
		// Create a parent node with a transform
		final Node parent = new Node("parent");
		parent.transform(new LocalTransform(Matrix.translation(Axis.Y)));

		// Attach a child node with a transform
		final Node child = new Node("child", parent);
		child.transform(transform);

		// Update and check combined world matrix
		final var expected = Matrix.translation(Axis.Y).multiply(Matrix.translation(Axis.X));
		transform.update(child);
		assertEquals(expected, transform.world());
	}

	@Test
	void mutable() {
		final var mutable = LocalTransform.mutable(new MutableRotation(Axis.Z));
		assertEquals(true, mutable.isDirty());
		mutable.update(new Node("whatever"));
		assertEquals(true, mutable.isDirty());
	}
}
