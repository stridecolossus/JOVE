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
		final DefaultModel cube = builder.size(2).build();
		final int count = 6 * 2 * 3;
		assertNotNull(cube);
		assertEquals(Primitive.TRIANGLES, cube.primitive());
		assertEquals(4, cube.layout().size());
		assertEquals(count, cube.count());
		assertEquals(count, cube.vertices().count());
	}
}
