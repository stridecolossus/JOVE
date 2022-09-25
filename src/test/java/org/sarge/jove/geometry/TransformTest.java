package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Matrix.Matrix4;

class TransformTest {
	@Test
	void compound() {
		final Transform trans = Matrix4.IDENTITY;
		final Transform compound = Transform.of(List.of(trans));
		assertNotNull(compound);
		assertEquals(false, compound.isMutable());
		assertEquals(Matrix4.IDENTITY, compound.matrix());
	}
}
