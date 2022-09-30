package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;
import org.sarge.jove.scene.LocalTransform.WorldMatrixVisitor;

public class LocalTransformTest {
	private LocalTransform transform;
	private Matrix matrix;

	@BeforeEach
	void before() {
		matrix = Matrix4.translation(Axis.X);
		transform = new LocalTransform(); //matrix);
	}

	@DisplayName("The world matrix for a new local transform is initially undefined")
	@Test
	void constructor() {
		assertEquals(matrix, transform.transform());
		assertEquals(true, transform.isDirty());
		assertEquals(null, transform.matrix());
		assertEquals(false, transform.isMutable());
	}

	@DisplayName("The world matrix of a local transform can be initialised")
	@Test
	void update() {
		transform.update(null);
		assertEquals(false, transform.isDirty());
		assertEquals(matrix, transform.matrix());
	}

	@DisplayName("The local transform can be composed with a parent transform")
	@Test
	void compose() {
		final LocalTransform parent = new LocalTransform(); //matrix);
		parent.update(null);
		transform.update(parent);
		assertEquals(false, transform.isDirty());
		assertEquals(matrix.multiply(matrix), transform.matrix());
	}

	@Nested
	class VisitorTests {
		private WorldMatrixVisitor visitor;

		@BeforeEach
		void before() {
			visitor = new WorldMatrixVisitor();
		}

		@DisplayName("The world matrix of a node is evaluated if it has not been initialised")
		@Test
		void update() {
			visitor.update(transform);
			assertEquals(false, transform.isDirty());
			assertEquals(matrix, transform.matrix());
		}

		@DisplayName("The world matrix of a node is composed with its ancestors")
		@Test
		void dirty() {
			transform.update(null);
			visitor.update(new LocalTransform()); //matrix));
			visitor.update(transform);
			assertEquals(false, transform.isDirty());
			assertEquals(matrix.multiply(matrix), transform.matrix());
		}
	}
}
