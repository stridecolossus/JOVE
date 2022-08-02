package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Ray.Intersection;

class VolumeTest {
	private Volume vol;

	@BeforeEach
	void before() {
		vol = spy(Volume.class);
	}

	@Test
	void intersectsDefault() {
		assertThrows(UnsupportedOperationException.class, () -> vol.intersects(Volume.EMPTY));
	}

	@Nested
	class EmptyVolumeTests {
		@Test
		void contains() {
			assertEquals(false, Volume.EMPTY.contains(null));
		}

		@Test
		void intersectsVolume() {
			assertEquals(false, Volume.EMPTY.intersects(Volume.EMPTY));
		}

		@Test
		void intersectsPlane() {
			assertEquals(false, Volume.EMPTY.intersects(new Plane(Vector.Y, 1)));
		}

		@Test
		void intersectsRay() {
			final Intersection intersection = Volume.EMPTY.intersect(null);
			assertNotNull(intersection);
			assertEquals(List.of(), intersection.distances());
		}

		@Test
		void equals() {
			assertEquals(true, Volume.EMPTY.equals(Volume.EMPTY));
			assertEquals(false, Volume.EMPTY.equals(null));
			assertEquals(false, Volume.EMPTY.equals(vol));
		}
	}
}
