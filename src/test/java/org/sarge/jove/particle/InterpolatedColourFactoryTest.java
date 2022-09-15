package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.lib.element.Element;

public class InterpolatedColourFactoryTest {
	@DisplayName("A colour factory can interpolate a colour based on particle age")
	@Test
	void interpolated() {
		final ColourFactory factory = new InterpolatedColourFactory(Colour.WHITE, Colour.BLACK);
		assertNotNull(factory);
		assertEquals(Colour.WHITE, factory.colour(0));
		assertEquals(Colour.BLACK, factory.colour(1));
		assertEquals(true, factory.isModified());
	}

	@Test
	void load() {
		final Element e = new Element.Builder()
				.child("start", "1 1 1")
				.child("end", "0 0 0")
				.build();
		final ColourFactory factory = InterpolatedColourFactory.load(e);
		assertNotNull(factory);
		assertEquals(Colour.WHITE, factory.colour(0));
		assertEquals(Colour.BLACK, factory.colour(1));
		assertEquals(true, factory.isModified());
	}
}
