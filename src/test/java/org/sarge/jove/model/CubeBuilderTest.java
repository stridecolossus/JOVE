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
		final Model cube = builder.build();
		assertNotNull(cube);
		assertEquals(Primitive.TRIANGLE_LIST, cube.primitive());
		assertEquals(new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE), cube.layout());
		assertEquals(2 * 3 * 6, cube.size());
	}

	// TODO - clockwise test
}
