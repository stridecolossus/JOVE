package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;

class InverseVolumeTest {
	private Volume inverse, vol;
	private Extents extents;

	@BeforeEach
	void before() {
		// Define extents
		extents = mock(Extents.class);
		when(extents.invert()).thenReturn(extents);

		// Create underlying volume
		vol = mock(Volume.class);
		when(vol.extents()).thenReturn(extents);

		// Create inverse volume
		inverse = new InverseVolume(vol);
	}

	@Test
	void constructor() {
		assertEquals(extents, inverse.extents());
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
