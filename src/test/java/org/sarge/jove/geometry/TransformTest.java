package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;

class TransformTest {
	@Test
	void compound() {
		final Transform trans = Matrix4.IDENTITY;
		final Transform compound = Transform.of(List.of(trans));
		assertNotNull(compound);
		assertEquals(false, compound.isDirty());
		assertEquals(Matrix4.IDENTITY, compound.matrix());
	}

	@Test
	void billboard() {
		// TODO
	}
}
