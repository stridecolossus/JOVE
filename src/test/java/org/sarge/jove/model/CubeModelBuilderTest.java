package org.sarge.jove.model;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CubeModelBuilderTest {
	private CubeModelBuilder builder;

	@BeforeEach
	public void before() {
		builder = new CubeModelBuilder();
	}

	@Test
	public void build() {
		final Model cube = builder.build(3);
		assertNotNull(cube);
		assertEquals(false, cube.isIndexed());
		assertEquals(Primitive.TRIANGLE, cube.primitive());
		assertEquals(Set.of(Vertex.Component.POSITION, Vertex.Component.NORMAL, Vertex.Component.TEXTURE_COORDINATE), cube.components());
		assertEquals(6 * 2 * 3, cube.vertices().size());
	}
}
