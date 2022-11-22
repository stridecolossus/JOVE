package org.sarge.jove.scene.particle;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Colour;
import org.sarge.jove.scene.particle.ColourFactory;

public class ColourFactoryTest {
	@DisplayName("A colour factory can specify a constant colour")
	@Test
	void of() {
		final ColourFactory factory = ColourFactory.of(Colour.WHITE);
		assertNotNull(factory);
		assertEquals(Colour.WHITE, factory.colour(0));
		assertEquals(false, factory.isModified());
	}
}
