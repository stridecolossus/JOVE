package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

class QuadTest {
	private Quad quad;

	@BeforeEach
	void before() {
		quad = new Quad(new Point(-1, -1, 0), new Point(+1, +1, 0));
	}

	@Test
	void centre() {
		assertEquals(Point.ORIGIN, quad.centre());
	}

	@Test
	void normal() {
		assertEquals(Axis.Z.invert(), quad.normal());
	}
}
