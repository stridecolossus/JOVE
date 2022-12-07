package org.sarge.jove.scene.volume;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersected;

class EmptyVolumeTest {
	private Volume vol;

	@BeforeEach
	void before() {
		vol = new EmptyVolume();
	}

	@Test
	void contains() {
		assertEquals(false, vol.contains(null));
	}

	@Test
	void intersectsVolume() {
		assertEquals(false, vol.intersects(vol));
	}

	@Test
	void intersectsPlane() {
		assertEquals(false, vol.intersects(new Plane(Axis.Y, 1)));
	}

	@Test
	void intersectsRay() {
		assertEquals(Intersected.NONE, vol.intersections(mock(Ray.class)));
	}

	@Test
	void equals() {
		assertEquals(vol, vol);
		assertEquals(vol, new EmptyVolume());
		assertNotEquals(vol, null);
		assertNotEquals(vol, mock(Volume.class));
	}
}
