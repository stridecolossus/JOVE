package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Tuple;
import org.sarge.jove.texture.TextureCoordinate;

public class QuadTest {
	@Test
	public void builder() {
		// Build a custom quad in the X-Z plane
		final Quad quad = new Quad.Builder()
			.size(3)
			.depth(-1)
			.swizzle(Tuple.Swizzle.XZ)
			.reverse()
			.build();

		// Check quad
		assertNotNull(quad);
		assertNotNull(quad.vertices());
		assertEquals(4, quad.vertices().size());

		// Check vertices
		final List<Vertex> vertices = quad.vertices();
		assertEquals(new Point(-3, +3, +3), vertices.get(0).position());
		assertEquals(new Point(-3, -3, +3), vertices.get(1).position());
		assertEquals(new Point(-3, +3, -3), vertices.get(2).position());
		assertEquals(new Point(-3, -3, -3), vertices.get(3).position());

		// Check texture coordinates
		assertEquals(TextureCoordinate.of(0, 1), vertices.get(0).coords());
		assertEquals(TextureCoordinate.of(0, 0), vertices.get(1).coords());
		assertEquals(TextureCoordinate.of(1, 1), vertices.get(2).coords());
		assertEquals(TextureCoordinate.of(1, 0), vertices.get(3).coords());
	}
}
