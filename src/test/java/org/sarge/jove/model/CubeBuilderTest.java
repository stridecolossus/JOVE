package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CubeBuilderTest {
	private CubeBuilder builder;

	@BeforeEach
	void before() {
		builder = new CubeBuilder();
	}

	@Test
	void build() {
		// Build cube
		final Model cube = builder.size(2).build();

		// Check model
		assertNotNull(cube);
		assertEquals(false, cube.isIndexed());

		// Check header
		final int count = 6 * 2 * 3;
		assertEquals(Primitive.TRIANGLES, cube.primitive());
		assertEquals(4, cube.layout().size());
		assertEquals(count, cube.count());
		assertEquals(false, cube.isIndexed());

		// Check vertices
		assertNotNull(cube.vertices());
		assertEquals(count * (3 + 3 + 2 + 4) * Float.BYTES, cube.vertices().length());
	}
}
