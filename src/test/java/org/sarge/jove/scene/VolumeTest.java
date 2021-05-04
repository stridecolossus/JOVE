package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Plane;
import org.sarge.jove.geometry.Ray.Intersection;

class VolumeTest {
	private Volume vol;

	@BeforeEach
	void before() {
		vol = spy(Volume.class);
	}

	@Test
	void intersectsDefault() {
		assertThrows(UnsupportedOperationException.class, () -> vol.intersects((Volume) null));
	}

	@Nested
	class NullVolumeTests {
		@Test
		void contains() {
			assertEquals(false, Volume.NULL.contains(null));
		}

		@Test
		void intersectsVolume() {
			assertEquals(false, Volume.NULL.intersects((Volume) null));
		}

		@Test
		void intersectsPlane() {
			assertEquals(false, Volume.NULL.intersects((Plane) null));
		}

		@Test
		void intersectsRay() {
			final Intersection intersection = Volume.NULL.intersect(null);
			assertNotNull(intersection);
			assertEquals(List.of(), intersection.distances());
		}

		@Test
		void equals() {
			assertEquals(true, Volume.NULL.equals(Volume.NULL));
			assertEquals(false, Volume.NULL.equals(null));
			assertEquals(false, Volume.NULL.equals(vol));
		}
	}
}
