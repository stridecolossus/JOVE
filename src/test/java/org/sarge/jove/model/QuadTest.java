package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class QuadTest {
	@Test
	void quad() {
		final Mesh mesh = Quad.build(1);
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(6, mesh.count());

		final int components = 3 + 3 + 2;
		final int vertices = 2 * 3;
		assertEquals(components * Float.BYTES * vertices, mesh.vertices().length());
	}
}
