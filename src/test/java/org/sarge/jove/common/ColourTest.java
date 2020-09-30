package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.sarge.jove.util.TestHelper.assertFloatArrayEquals;
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
		assertEquals(0.1f, col.red(), 0.0001f);
		assertEquals(0.2f, col.green(), 0.0001f);
		assertEquals(0.3f, col.blue(), 0.0001f);
		assertEquals(1.0f, col.alpha(), 0.0001f);
	}

	@Test
	public void arrayConstructor() {
		assertEquals(col, new Colour(new float[]{col.red(), col.green(), col.blue(), 1f}));
		assertEquals(col, new Colour(new float[]{col.red(), col.green(), col.blue()}));
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
		assertFloatEquals(col.red(), buffer.get());
		assertFloatEquals(col.green(), buffer.get());
		assertFloatEquals(col.blue(), buffer.get());
		assertFloatEquals(col.alpha(), buffer.get());
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
