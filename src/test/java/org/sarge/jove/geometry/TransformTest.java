package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;

class TransformTest {
	@Test
	void compound() {
		final Transform trans = Matrix.IDENTITY;
		final Transform compound = Transform.of(List.of(trans));
		assertNotNull(compound);
		assertEquals(false, compound.isDirty());
		assertEquals(Matrix.IDENTITY, compound.matrix());
	}
}
