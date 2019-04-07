package org.sarge.jove.model;

import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Tuple.Swizzle;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.MutableVertex;
import org.sarge.jove.texture.TextureCoordinate.Coordinate2D;

public class QuadTest {
	@Test
	public void builder() {
		// Build a backwards quad in the X-Z plane
		final Quad quad = new Quad.Builder()
			.size(3)
			.depth(4)
			.reverse()
			.swizzle(Swizzle.YZ)
			.build();

		// Check quad
		assertNotNull(quad);
		assertNotNull(quad.vertices());
		assertEquals(4, quad.vertices().size());

		// Check vertices
		final var vertices = quad.vertices();
		assertEquals(new Point(+3, 4, -3), vertices.get(0).position());
		assertEquals(new Point(+3, 4, +3), vertices.get(1).position());
		assertEquals(new Point(-3, 4, -3), vertices.get(2).position());
		assertEquals(new Point(-3, 4, +3), vertices.get(3).position());

		// Check normals
		for(MutableVertex v : vertices) {
			assertEquals(Vector.Y_AXIS.invert(), v.normal());
		}

		// Check texture coordinates
		assertEquals(Coordinate2D.QUAD, vertices.stream().map(MutableVertex::coordinates).collect(toList()));
	}
}
