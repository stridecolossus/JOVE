package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Tuple.Swizzle;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.model.Vertex.MutableVertex;

public class QuadTest {
	@Test
	public void constructor() {
		// Build quad points
		final Point[] array = new Point[4];
		Arrays.fill(array, Point.ORIGIN);

		// Create quad
		final Quad quad = new Quad(array, Vector.X_AXIS);
		final var vertices = quad.vertices();
		assertNotNull(vertices);

		// Check normals
		for(MutableVertex v : vertices) {
			assertEquals(Vector.X_AXIS, v.normal());
		}

		// Check texture coordinates
		for(int n = 0; n < 4; ++n) {
			assertEquals(Coordinate2D.QUAD.get(n), vertices.get(n).coordinates());
		}
	}

	@Test
	public void constructorInvalidVertices() {
		assertThrows(IllegalArgumentException.class, () -> new Quad(new Point[]{}, Vector.X_AXIS));
	}

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
		for(int n = 0; n < 4; ++n) {
			assertEquals(Coordinate2D.QUAD.get(n), vertices.get(n).coordinates());
		}
	}
}
