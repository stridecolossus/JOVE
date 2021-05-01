package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

class CompoundVolumeTest {
	private Volume vol;
	private CompoundVolume compound;

	@BeforeEach
	void before() {
		vol = mock(Volume.class);
		compound = new CompoundVolume(List.of(vol, vol));
	}

	@Test
	void of() {
		assertEquals(compound, CompoundVolume.of(vol, vol));
	}

	@Test
	void contains() {
		when(vol.contains(Point.ORIGIN)).thenReturn(true);
		assertEquals(true, compound.contains(Point.ORIGIN));
	}

	@Test
	void intersects() {
		assertEquals(false, compound.intersects(mock(Volume.class)));
	}

	@Test
	void intersectsDelegate() {
		final Volume other = mock(Volume.class);
		when(vol.intersects(other)).thenReturn(true);
		assertEquals(true, compound.intersects(other));
	}

	@Test
	void intersectsRay() {
		// TODO
	}

	@Test
	void equals() {
		assertEquals(true, compound.equals(compound));
		assertEquals(true, compound.equals(new CompoundVolume(List.of(vol, vol))));
		assertEquals(false, compound.equals(null));
		assertEquals(false, compound.equals(new CompoundVolume(List.of(vol))));
	}
}
