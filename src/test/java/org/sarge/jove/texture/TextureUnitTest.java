package org.sarge.jove.texture;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TextureUnitTest {
	private TextureUnit.Factory factory;

	@BeforeEach
	public void before() {
		factory = new TextureUnit.Factory(1);
	}

	@Test
	public void constructor() {
		assertEquals(1, factory.max());
	}

	@Test
	public void unit() {
		final TextureUnit unit = factory.unit(0);
		assertNotNull(unit);
		assertEquals(0, unit.index());
	}

	@Test
	public void unitInvalid() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> factory.unit(1));
	}

	@Test
	public void equals() {
		final TextureUnit unit = TextureUnit.DEFAULT.unit(0);
		assertEquals(true, unit.equals(unit));
		assertEquals(false, unit.equals(null));
		assertEquals(false, unit.equals(TextureUnit.DEFAULT.unit(1)));
	}
}
