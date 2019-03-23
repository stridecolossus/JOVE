package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RayTest {
	private Ray ray;

	@BeforeEach
	public void before() {
		ray = new Ray(Point.ORIGIN, Vector.X_AXIS);
	}

	@Test
	public void constructor() {
		assertEquals(Point.ORIGIN, ray.origin());
		assertEquals(Vector.X_AXIS, ray.direction());
	}
}
