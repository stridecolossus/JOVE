package org.sarge.jove.scene.volume;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

class CompoundVolumeTest {
	private Volume vol;
	private CompoundVolume compound;

	@BeforeEach
	void before() {
		vol = new SphereVolume(Point.ORIGIN, 1);
		compound = new CompoundVolume(List.of(vol, vol));
	}

	@Test
	void contains() {
		assertEquals(true, compound.contains(Point.ORIGIN));
	}

	@Test
	void intersects() {
//		assertEquals(false, compound.intersects(mock(Volume.class)));
	}

	@Test
	void intersectsRay() {
		// TODO
	}
}
