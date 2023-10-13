package org.sarge.jove.scene.volume;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Ray.Intersection;

class VolumeTest {
	private Volume vol;

	@BeforeEach
	void before() {
		//vol = spy(Volume.class);
		vol = new Volume() {
			@Override
			public Iterable<Intersection> intersections(Ray ray) {
				return null;
			}

			@Override
			public boolean intersects(Plane plane) {
				return false;
			}

			@Override
			public boolean contains(Point p) {
				return false;
			}

			@Override
			public Bounds bounds() {
				return null;
			}
		};
	}

	@Test
	void intersectsDefault() {
		assertThrows(UnsupportedOperationException.class, () -> vol.intersects(vol)); // mock(Volume.class)));
	}
}
// TODO - is there much point in this test?
