package org.sarge.jove.scene.volume;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersected;

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
		assertEquals(Intersected.NONE, EmptyVolume.INSTANCE.intersections(mock(Ray.class)));
	}

	@Test
	void equals() {
		assertEquals(EmptyVolume.INSTANCE, EmptyVolume.INSTANCE);
		assertNotEquals(EmptyVolume.INSTANCE, null);
		assertNotEquals(EmptyVolume.INSTANCE, mock(Volume.class));
	}
}
