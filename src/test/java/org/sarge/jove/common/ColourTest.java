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
		assertEquals(Layout.floats(4), Colour.LAYOUT);
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

//	@DisplayName("A colour can be interpolated between two colours")
//	@Test
//	void interpolator() {
//		final FloatFunction<Colour> interpolator = Colour.interpolator(Colour.WHITE, Colour.BLACK, Interpolator.LINEAR);
//		assertNotNull(interpolator);
//		assertEquals(Colour.WHITE, interpolator.apply(0));
//		assertEquals(Colour.BLACK, interpolator.apply(1));
//	}

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
		final float[] expected = {0.1f, 0.2f, 0.3f, 1};
		assertArrayEquals(expected, col.toArray());
	}

	@Test
	void equals() {
		assertEquals(col, col);
		assertEquals(col, new Colour(0.1f, 0.2f, 0.3f, 1f));
		assertNotEquals(col, null);
		assertNotEquals(col, Colour.WHITE);
	}
}
