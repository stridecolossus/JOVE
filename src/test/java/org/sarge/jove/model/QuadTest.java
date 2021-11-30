package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;

public class QuadTest {
	@Test
	void coordinates() {
		assertEquals(List.of(Coordinate2D.TOP_LEFT, Coordinate2D.BOTTOM_LEFT, Coordinate2D.TOP_RIGHT, Coordinate2D.BOTTOM_RIGHT), Quad.COORDINATES);
	}

	@Test
	void stripTriangleStrip() {
		final IntStream strip = Quad.strip(2, true);
		assertNotNull(strip);
		assertArrayEquals(new int[]{0, 1, 2, 1, 2, 3, 2, 3, 4, 3, 4, 5}, strip.toArray());
	}

	@Test
	void stripTriangles() {
		final IntStream triangles = Quad.strip(2, false);
		assertNotNull(triangles);
		assertArrayEquals(new int[]{0, 1, 2, 2, 1, 3, 2, 3, 4, 4, 3, 5}, triangles.toArray());
	}

	@Test
	void triangle() {
		final IntStream triangles = Quad.triangle(1);
		assertNotNull(triangles);
		assertArrayEquals(new int[]{1, 2, 3}, triangles.toArray());
	}

	@Test
	void clockwise() {
		final IntStream triangles = Quad.clockwise(1);
		assertNotNull(triangles);
		assertArrayEquals(new int[]{2, 1, 3}, triangles.toArray());
	}
}
