package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.FloatBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColourTest {
	private Colour col;

	@BeforeEach
	public void before() {
		col = new Colour(0.1f, 0.2f, 0.3f, 1f);
	}

	@Test
	public void constructor() {
		assertEquals(0.1f, col.r, 0.0001f);
		assertEquals(0.2f, col.g, 0.0001f);
		assertEquals(0.3f, col.b, 0.0001f);
		assertEquals(1.0f, col.a, 0.0001f);
		assertEquals(4, col.size());
	}

	@Test
	public void arrayConstructor() {
		assertEquals(col, new Colour(new float[]{col.r, col.g, col.b, 1f}));
		assertEquals(col, new Colour(new float[]{col.r, col.g, col.b}));
	}

	@Test
	public void constructorInvalid() {
		assertThrows(IllegalArgumentException.class, () -> new Colour(0, 0, 0, 999));
	}

	@Test
	public void buffer() {
		final FloatBuffer buffer = FloatBuffer.allocate(4);
		col.buffer(buffer);
		buffer.flip();
		assertFloatEquals(col.r, buffer.get());
		assertFloatEquals(col.g, buffer.get());
		assertFloatEquals(col.b, buffer.get());
		assertFloatEquals(col.a, buffer.get());
	}

	@Test
	public void pixel() {
		final int pixel = Colour.WHITE.toPixel();
		assertEquals(Colour.WHITE, Colour.of(pixel));
	}

	@Test
	public void equals() {
		assertTrue(col.equals(col));
		assertFalse(col.equals(null));
		assertFalse(col.equals(Colour.WHITE));
	}
}
