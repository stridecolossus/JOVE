package org.sarge.jove.scene.volume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

class InverseVolumeTest {
	private Volume inverse, vol;

	@BeforeEach
	void before() {
		inverse = new InverseVolume(new SphereVolume(Point.ORIGIN, 1));
	}

	@Test
	void contains() {
		assertEquals(true, inverse.contains(Point.ORIGIN));
	}

	@Test
	void intersects() {
//		final Volume other = mock(Volume.class);
//		assertEquals(true, inverse.intersects(other));
//		verify(vol).intersects(other);
	}

	// TODO
	// - intersect ray
}
