package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Vector;

public class LocalTransformTest {
	private LocalTransform transform;
	private Matrix world;

	@BeforeEach
	public void before() {
		world = Matrix.translation(Vector.X_AXIS);
		transform = new LocalTransform(Matrix.translation(Vector.Y_AXIS));
	}

	@Test
	public void constructor() {
		assertEquals(Matrix.translation(Vector.Y_AXIS), transform.transform());
		assertEquals(null, transform.matrix());
	}

	@Test
	public void update() {
		final Matrix expected = Matrix.translation(Vector.X_AXIS).multiply(Matrix.translation(Vector.Y_AXIS));
		transform.update(world);
		assertEquals(expected, transform.matrix());
	}

	@Test
	public void none() {
		transform = LocalTransform.none();
		transform.update(world);
		assertEquals(world, transform.matrix());
		assertEquals(Matrix.IDENTITY, transform.transform());
	}

	@Test
	public void visitor() {
		// TODO - check child multiplied by parent
		final LocalTransform.Visitor visitor = new LocalTransform.Visitor();
		final Node node = new Node("node");
		node.transform(transform);
		visitor.visit(node);
		transform.update(world);
		assertNotNull(transform.matrix());
	}
}
