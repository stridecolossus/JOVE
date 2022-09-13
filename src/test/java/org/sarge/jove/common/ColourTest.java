package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;

class ColourTest {
	private Colour col;

	@BeforeEach
	void before() {
		col = new Colour(0.1f, 0.2f, 0.3f, 1f);
	}

	@DisplayName("A colour is comprised of RGBA components")
	@Test
	void constructor() {
		assertEquals(0.1f, col.red());
		assertEquals(0.2f, col.green());
		assertEquals(0.3f, col.blue());
		assertEquals(1.0f, col.alpha());
	}

	@DisplayName("A colour has a 4-component layout")
	@Test
	void layout() {
		assertEquals(Component.floats(4), Colour.LAYOUT);
		assertEquals(4 * Float.BYTES, col.length());
	}

	@Nested
	class ArrayConstructorTests {
		@DisplayName("A colour can be constructed from a RGBA array")
		@Test
		void constructor() {
			assertEquals(col, Colour.of(new float[]{col.red(), col.green(), col.blue(), 1f}));
		}

		@DisplayName("A colour can be constructed from a RGB array with an implicit full alpha channel")
		@Test
		void alpha() {
			assertEquals(col, Colour.of(new float[]{col.red(), col.green(), col.blue()}));
		}

		@DisplayName("A colour cannot be constructed from an empty array")
		@Test
		void empty() {
			assertThrows(IllegalArgumentException.class, () -> Colour.of(new float[0]));
		}

		@DisplayName("A colour cannot be constructed from an array that does not contain RGB(A) components")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> Colour.of(new float[]{1}));
		}
	}

	@Test
	void interpolate() {
		assertEquals(new Colour(0.5f, 0.5f, 0.5f, 1), Colour.BLACK.interpolate(Colour.WHITE, 0.5f));
		assertEquals(Colour.BLACK, Colour.BLACK.interpolate(Colour.WHITE, 0));
		assertEquals(Colour.WHITE, Colour.BLACK.interpolate(Colour.WHITE, 1));
	}

	@DisplayName("A colour has a length in bytes")
	@Test
	void length() {
		assertEquals(4 * Float.BYTES, col.length());
	}

	@DisplayName("A colour can be written to an NIO buffer")
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

	@DisplayName("A colour can be converted to an RGBA array")
	@Test
	void toArray() {
		final float[] array = col.toArray();
		assertArrayEquals(new float[]{0.1f, 0.2f, 0.3f, 1}, array);
	}

	@Test
	void equals() {
		assertEquals(col, col);
		assertEquals(col, new Colour(0.1f, 0.2f, 0.3f, 1f));
		assertNotEquals(col, null);
		assertNotEquals(col, Colour.WHITE);
	}
}
