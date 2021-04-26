package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

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
			assertEquals(Optional.empty(), Volume.NULL.intersect(null));
		}

		@Test
		void equals() {
			assertEquals(true, Volume.NULL.equals(Volume.NULL));
			assertEquals(false, Volume.NULL.equals(null));
		}
	}
}
