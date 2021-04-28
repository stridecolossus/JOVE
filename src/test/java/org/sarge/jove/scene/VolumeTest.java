package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Ray.Intersection;

class VolumeTest {
	@Test
	void intersectsDefault() {
		final Volume vol = spy(Volume.class);
		assertThrows(UnsupportedOperationException.class, () -> vol.intersects(null));
	}

	@Nested
	class NullVolumeTests {
		@Test
		void extents() {
			assertNotNull(Volume.NULL.extents());
			assertEquals(true, Volume.NULL.extents().contains(Point.ORIGIN));
		}

		@Test
		void contains() {
			assertEquals(false, Volume.NULL.contains(null));
		}

		@Test
		void intersects() {
			assertEquals(false, Volume.NULL.intersects(null));
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
		}
	}
}
