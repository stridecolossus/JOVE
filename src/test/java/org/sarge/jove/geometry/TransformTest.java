package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class TransformTest {
	@Test
	void compound() {
		final Transform trans = Matrix.IDENTITY;
		final Transform compound = Transform.of(List.of(trans));
		assertEquals(Matrix.IDENTITY, compound.matrix());
	}
}
