package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.util.Randomiser;
import org.sarge.lib.element.Element;

public class DiscTest {
	private Disc disc;
	private Randomiser randomiser;
	private float next;

	@BeforeEach
	void before() {
		randomiser = new Randomiser() {
			@Override
			public float next() {
				return next;
			}
		};
		disc = new Disc(Axis.Y, 1, randomiser);
	}

	@Test
	void origin() {
		next = 0.5f;
		assertEquals(Point.ORIGIN, disc.point());
		assertEquals(Axis.Y, disc.vector());
	}

	@Test
	void radius() {
		next = 1;
		assertEquals(new Point(1, 0, 1), disc.point());
		assertEquals(new Vector(1, 1, 1).normalize(), disc.vector());
	}

	@Test
	void load() {
		final Element e = new Element.Builder()
				.child("normal", "0 1 0")
				.child("radius", "0")
				.build();

		disc = Disc.load(e, randomiser);
		assertNotNull(disc);

		next = 0.5f;
		assertEquals(Axis.Y, disc.vector());
	}
}
