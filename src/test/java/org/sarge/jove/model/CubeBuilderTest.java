package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Model.Header;

public class CubeBuilderTest {
	private CubeBuilder builder;

	@BeforeEach
	void before() {
		builder = new CubeBuilder();
	}

	@Test
	void build() {
		final VertexModel cube = builder.build();
		assertNotNull(cube);
		assertEquals(new Header(Primitive.TRIANGLES, false, (2 * 3) * 6), cube.header());
		assertEquals(new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.COORDINATE), cube.layout());
	}
}
