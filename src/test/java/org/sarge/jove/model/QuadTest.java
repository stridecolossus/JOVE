package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;

public class QuadTest {
	@Test
	void triangles() {
		assertEquals(List.of(0, 1, 2), Quad.LEFT);
		assertEquals(List.of(2, 1, 3), Quad.RIGHT);
	}

	@Test
	void coordinates() {
		assertEquals(List.of(Coordinate2D.TOP_LEFT, Coordinate2D.BOTTOM_LEFT, Coordinate2D.TOP_RIGHT, Coordinate2D.BOTTOM_RIGHT), Quad.COORDINATES);
	}
}
