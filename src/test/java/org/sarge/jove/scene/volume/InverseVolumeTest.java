package org.sarge.jove.scene.volume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

class InverseVolumeTest {
	private Volume inverse;

	@BeforeEach
	void before() {
		inverse = new InverseVolume(new SphereVolume(Point.ORIGIN, 1));
	}

	@Test
	void contains() {
		assertEquals(false, inverse.contains(Point.ORIGIN));
	}

	@Test
	void intersects() {
		assertEquals(false, inverse.intersects(new SphereVolume(Point.ORIGIN, 1)));
	}
}
