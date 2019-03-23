package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

public class TransformTest {
	@Test
	public void compound() {
		final Matrix x = Matrix.translation(Vector.X_AXIS);
		final Matrix y = Matrix.translation(Vector.Y_AXIS);
		final Transform compound = Transform.of(List.of(x, y));
		assertEquals(x.multiply(y), compound.matrix());
	}

	@Test
	public void billboard() {
	}
}
