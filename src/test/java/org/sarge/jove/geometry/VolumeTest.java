package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.spy;

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
			assertEquals(false, Volume.EMPTY.intersects(new Plane(Axis.Y.vector(), 1)));
		}

		@Test
		void intersectsRay() {
			assertEquals(Intersection.NONE, Volume.EMPTY.intersection(null));
		}

		@Test
		void equals() {
			assertEquals(Volume.EMPTY, Volume.EMPTY);
			assertNotEquals(Volume.EMPTY, null);
			assertNotEquals(Volume.EMPTY, vol);
		}
	}
}
