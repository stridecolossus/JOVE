package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sarge.jove.util.TestHelper.assertFloatArrayEquals;
import static org.sarge.jove.util.TestHelper.assertFloatEquals;

import java.nio.ByteBuffer;

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
		assertEquals(0.1f, col.red());
		assertEquals(0.2f, col.green());
		assertEquals(0.3f, col.blue());
		assertEquals(1.0f, col.alpha());
		assertEquals(4, Colour.SIZE);
		assertEquals(4 * Float.BYTES, col.length());
	}

	@Test
	public void array() {
		assertEquals(col, Colour.of(new float[]{col.red(), col.green(), col.blue(), 1f}));
		assertEquals(col, Colour.of(new float[]{col.red(), col.green(), col.blue()}));
	}

	@Test
	public void constructorInvalid() {
		assertThrows(IllegalArgumentException.class, () -> new Colour(0, 0, 0, 999));
	}

	@Test
	public void buffer() {
		final ByteBuffer buffer = ByteBuffer.allocate(4 * Float.BYTES);
		col.buffer(buffer);
		buffer.flip();
		assertFloatEquals(col.red(), buffer.getFloat());
		assertFloatEquals(col.green(), buffer.getFloat());
		assertFloatEquals(col.blue(), buffer.getFloat());
		assertFloatEquals(col.alpha(), buffer.getFloat());
	}

	@Test
	void toArray() {
		final float[] array = col.toArray();
		assertFloatArrayEquals(new float[]{0.1f, 0.2f, 0.3f, 1}, array);
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
