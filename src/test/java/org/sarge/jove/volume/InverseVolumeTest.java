package org.sarge.jove.volume;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

class InverseVolumeTest {
	private Volume inverse, vol;

	@BeforeEach
	void before() {
		vol = mock(Volume.class);
		inverse = new InverseVolume(vol);
	}

	@Test
	void contains() {
		assertEquals(true, inverse.contains(Point.ORIGIN));
		verify(vol).contains(Point.ORIGIN);
	}

	@Test
	void intersects() {
		final Volume other = mock(Volume.class);
		assertEquals(true, inverse.intersects(other));
		verify(vol).intersects(other);
	}

	// TODO
	// - intersect ray

	@Test
	void equals() {
		assertEquals(true, inverse.equals(inverse));
		assertEquals(true, inverse.equals(new InverseVolume(vol)));
		assertEquals(false, inverse.equals(null));
		assertEquals(false, inverse.equals(mock(Volume.class)));
	}
}
