package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.lib.element.Element;
import org.sarge.lib.element.Element.ElementException;

public class InterpolatorTest {
	@Test
	void identity() {
		assertEquals(0, Interpolator.LINEAR.apply(0));
		assertEquals(0.5f, Interpolator.LINEAR.apply(0.5f));
		assertEquals(1, Interpolator.LINEAR.apply(1));
	}

	@Test
	void quadratic() {
		assertEquals(0, Interpolator.QUADRATIC.apply(0));
		assertEquals(0.25f, Interpolator.QUADRATIC.apply(0.5f));
		assertEquals(1, Interpolator.QUADRATIC.apply(1));
	}

	@Test
	void exponential() {
		final Interpolator exp = Interpolator.exponential(2);
		assertNotNull(exp);
		assertEquals(0.25f, exp.apply(0.5f));
	}

	@Test
	void smooth() {
		assertEquals(0, Interpolator.SMOOTH.apply(0));
		assertEquals(0.5f, Interpolator.SMOOTH.apply(0.5f));
		assertEquals(1, Interpolator.SMOOTH.apply(1));
	}

	@Test
	void invert() {
		final Interpolator inv = Interpolator.LINEAR.invert();
		assertNotNull(inv);
		assertEquals(1, inv.apply(0));
		assertEquals(0.5f, inv.apply(0.5f));
		assertEquals(0, inv.apply(1));
	}

	@DisplayName("A mirror interpolator reflects the function about time axis")
	@Test
	void mirror() {
		final Interpolator mirror = Interpolator.LINEAR.mirror();
		assertNotNull(mirror);
		assertEquals(0, mirror.apply(0));
		assertEquals(1, mirror.apply(0.5f));
		assertEquals(0, mirror.apply(1));
	}

	@Test
	void range() {
		final Interpolator range = Interpolator.LINEAR.range(-1, +1);
		assertNotNull(range);
		assertEquals(-1, range.apply(0));
		assertEquals(0, range.apply(0.5f));
		assertEquals(+1, range.apply(1));
	}

	@Test
	void linear() {
		final Interpolator linear = Interpolator.linear(-1, +1);
		assertNotNull(linear);
		assertEquals(-1, linear.apply(0));
		assertEquals(0, linear.apply(0.5f));
		assertEquals(+1, linear.apply(1));
	}

	@Test
	void lerp() {
		assertEquals(1, Interpolator.lerp(0, 1, 3));
		assertEquals(2, Interpolator.lerp(0.5f, 1, 3));
		assertEquals(3, Interpolator.lerp(1, 1, 3));
	}

	@Test
	void mix() {
		final Interpolator mix = Interpolator.mix(Interpolator.LINEAR, Interpolator.LINEAR.invert());
		assertNotNull(mix);
		assertEquals(0.5f, mix.apply(0));
		assertEquals(0.5f, mix.apply(0.5f));
		assertEquals(0.5f, mix.apply(1));
	}

	@Nested
	class LoaderTests {
		@Test
		void identity() {
			assertEquals(Interpolator.LINEAR, Interpolator.load(new Element("identity")));
		}

		@Test
		void quadratic() {
			assertEquals(Interpolator.QUADRATIC, Interpolator.load(new Element("quadratic")));
			assertEquals(Interpolator.QUADRATIC, Interpolator.load(new Element("squared")));
		}

		@Test
		void smooth() {
			assertEquals(Interpolator.SMOOTH, Interpolator.load(new Element("smooth")));
			assertEquals(Interpolator.SMOOTH, Interpolator.load(new Element("smoothstep")));
		}

		@Test
		void linear() {
			final Element e = new Element.Builder()
					.name("linear")
					.child("start", "1")
					.child("end", "2")
					.build();

			final Interpolator linear = Interpolator.load(e);
			assertEquals(1, linear.apply(0));
			assertEquals(2, linear.apply(1));
		}

		@Test
		void mix() {
			final Element e = new Element.Builder()
					.name("mix")
					.child(new Element("identity"))
					.child(new Element("identity"))
					.child("weight", "0.5")
					.build();

			final Interpolator mix = Interpolator.load(e);
			assertNotNull(mix);
		}

		@Test
		void unknown() {
			assertThrows(ElementException.class, () -> Interpolator.load(new Element("cobblers")));
		}
	}
}

