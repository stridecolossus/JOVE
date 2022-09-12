package org.sarge.jove.particle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;

public class ColourFactoryTest {
	@DisplayName("A colour factory can specify a constant colour")
	@Test
	void of() {
		final ColourFactory factory = ColourFactory.of(Colour.WHITE);
		assertNotNull(factory);
		assertEquals(Colour.WHITE, factory.colour(0));
		assertEquals(false, factory.isModified());
	}

	@DisplayName("A colour factory can interpolate a colour based on particle age")
	@Test
	void interpolated() {
		final ColourFactory factory = ColourFactory.interpolated(Colour.WHITE, Colour.BLACK);
		assertNotNull(factory);
		assertEquals(Colour.WHITE, factory.colour(0));
		assertEquals(Colour.BLACK, factory.colour(1));
		assertEquals(true, factory.isModified());
	}
}
