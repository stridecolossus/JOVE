package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ColourTest {
	private Colour col;

	@BeforeEach
	void before() {
		col = new Colour(0.1f, 0.2f, 0.3f, 1f);
	}

	@Test
	void constructor() {
		assertEquals(0.1f, col.red());
		assertEquals(0.2f, col.green());
		assertEquals(0.3f, col.blue());
		assertEquals(1.0f, col.alpha());
	}

	@Test
	void layout() {
		assertEquals(Layout.of(4), col.layout());
	}

	@Test
	void array() {
		assertEquals(col, Colour.of(new float[]{col.red(), col.green(), col.blue(), 1f}));
		assertEquals(col, Colour.of(new float[]{col.red(), col.green(), col.blue()}));
	}

	@Test
	void constructorInvalid() {
		assertThrows(IllegalArgumentException.class, () -> new Colour(0, 0, 0, 999));
	}

	@Test
	void buffer() {
		final ByteBuffer buffer = ByteBuffer.allocate(4 * Float.BYTES);
		col.buffer(buffer);
		buffer.flip();
		assertEquals(col.red(), buffer.getFloat());
		assertEquals(col.green(), buffer.getFloat());
		assertEquals(col.blue(), buffer.getFloat());
		assertEquals(col.alpha(), buffer.getFloat());
	}

	@Test
	void toArray() {
		final float[] array = col.toArray();
		assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f, 1}, array);
	}

	@Test
	void pixel() {
		final int pixel = Colour.WHITE.toPixel();
		assertEquals(Colour.WHITE, Colour.of(pixel));
	}

	@Test
	void equals() {
		assertTrue(col.equals(col));
		assertFalse(col.equals(null));
		assertFalse(col.equals(Colour.WHITE));
	}
}
