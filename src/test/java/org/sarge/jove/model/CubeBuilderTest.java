package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Component;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.model.Vertex.Layout;

public class CubeBuilderTest {
	private CubeBuilder builder;

	@BeforeEach
	void before() {
		builder = new CubeBuilder();
	}

	@Test
	void build() {
		final Layout expected = new Layout(Component.POSITION, Component.COORDINATE);
		final Model cube = builder.build();
		assertNotNull(cube);
		assertEquals(new Header(Primitive.TRIANGLES, expected, false), cube.header());
		assertEquals((2 * 3) * 6, cube.count());
		assertEquals(false, cube.isIndexed());
	}
}
