package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;

public class LocalTransformTest {
	private LocalTransform transform;
	private Matrix matrix;

	@BeforeEach
	void before() {
		matrix = Matrix4.translation(Axis.X.vector());
		transform = new LocalTransform();
	}

	@DisplayName("A new local transform...")
	@Nested
	class New {
		@DisplayName("is initialised to the identity matrix")
		@Test
		void constructor() {
			assertEquals(Matrix4.IDENTITY, transform.transform());
		}

		@DisplayName("can override the local transform")
		@Test
		void set() {
			transform.set(matrix);
			assertEquals(matrix, transform.transform());
		}

		@DisplayName("has an undefined world matrix")
		@Test
		void undefined() {
			assertEquals(true, transform.isDirty());
			assertThrows(IllegalStateException.class, () -> transform.matrix());
		}
	}

	@DisplayName("The world matrix of a new local transform...")
	@Nested
	class Undefined {
		@DisplayName("can be updated")
		@Test
		void update() {
			transform.set(matrix);
			transform.update(null);
			assertEquals(false, transform.isDirty());
			assertEquals(matrix, transform.matrix());
		}

		@DisplayName("can be composed with its parent")
		@Test
		void compose() {
			final LocalTransform parent = new LocalTransform();
			parent.set(matrix);
			parent.update(null);
			transform.update(parent);
			assertEquals(false, transform.isDirty());
			assertEquals(matrix, transform.matrix());
		}

		@DisplayName("silently ignores resets")
		@Test
		void reset() {
			transform.reset();
			assertEquals(true, transform.isDirty());
		}
	}

	@DisplayName("The world matrix of an updated local transform...")
	@Nested
	class Updated {
		@BeforeEach
		void before() {
			transform.update(null);
		}

		@DisplayName("can be modified")
		@Test
		void set() {
			transform.set(matrix);
			assertEquals(true, transform.isDirty());
		}

		@DisplayName("can be reset to the undefined state")
		@Test
		void reset() {
			transform.reset();
			assertEquals(true, transform.isDirty());
			assertThrows(IllegalStateException.class, () -> transform.matrix());
		}
	}

	@DisplayName("A local transform can be cloned")
	@Test
	void copy() {
		final LocalTransform copy = new LocalTransform(transform);
		assertEquals(true, copy.isDirty());
		assertEquals(Matrix4.IDENTITY, copy.transform());
	}
}
