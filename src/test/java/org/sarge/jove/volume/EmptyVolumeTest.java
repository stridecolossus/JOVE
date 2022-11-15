package org.sarge.jove.volume;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

class EmptyVolumeTest {
	@Test
	void contains() {
		assertEquals(false, EmptyVolume.INSTANCE.contains(null));
	}

	@Test
	void intersectsVolume() {
		assertEquals(false, EmptyVolume.INSTANCE.intersects(EmptyVolume.INSTANCE));
	}

	@Test
	void intersectsPlane() {
		assertEquals(false, EmptyVolume.INSTANCE.intersects(new Plane(Axis.Y, 1)));
	}

	@Test
	void intersectsRay() {
		assertEquals(Intersection.NONE, EmptyVolume.INSTANCE.intersection(null));
	}

	@Test
	void equals() {
		assertEquals(EmptyVolume.INSTANCE, EmptyVolume.INSTANCE);
		assertNotEquals(EmptyVolume.INSTANCE, null);
		assertNotEquals(EmptyVolume.INSTANCE, mock(Volume.class));
	}
}
