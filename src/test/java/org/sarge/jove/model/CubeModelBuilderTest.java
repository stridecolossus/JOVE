package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Vertex.MutableVertex;

public class CubeModelBuilderTest {
	private CubeModelBuilder builder;

	@BeforeEach
	public void before() {
		builder = new CubeModelBuilder();
	}

	@Test
	public void build() {
		final Model<MutableVertex> cube = builder.build(3);
		assertNotNull(cube);
		assertEquals(false, cube.isIndexed());
		assertEquals(Primitive.TRIANGLE_LIST, cube.primitive());
		assertEquals(List.of(Vertex.Component.POSITION, Vertex.Component.NORMAL, Vertex.Component.coordinate(2)), cube.components());
//		assertEquals(6 * 2 * 3, cube.vertices().size());
	}
}
