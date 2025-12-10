package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;

class ColourTest {
	private Colour colour;

	@BeforeEach
	void before() {
		colour = new Colour(0.1f, 0.2f, 0.3f);
	}

	@DisplayName("A colour is comprised of RGBA components")
	@Test
	void constructor() {
		assertEquals(0.1f, colour.red());
		assertEquals(0.2f, colour.green());
		assertEquals(0.3f, colour.blue());
		assertEquals(1.0f, colour.alpha());
	}

	@DisplayName("A colour has a 4-component layout")
	@Test
	void layout() {
		assertEquals(new Layout(4, Layout.Type.NORMALIZED, false, Float.BYTES), Colour.LAYOUT);
	}

	@Nested
	class ArrayConstructorTests {
		@DisplayName("A colour can be constructed from a RGBA array")
		@Test
		void constructor() {
			assertEquals(colour, Colour.of(new float[]{0.1f, 0.2f, 0.3f, 1}));
		}

		@DisplayName("A colour can be constructed from a RGB array with an implicit full alpha channel")
		@Test
		void alpha() {
			assertEquals(colour, Colour.of(new float[]{0.1f, 0.2f, 0.3f}));
		}

		@DisplayName("A colour cannot be constructed from an array that does not contain RGB(A) components")
		@Test
		void invalid() {
			assertThrows(IllegalArgumentException.class, () -> Colour.of(new float[0]));
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
		colour.buffer(buffer);
		buffer.flip();
		assertEquals(0.1f, buffer.getFloat());
		assertEquals(0.2f, buffer.getFloat());
		assertEquals(0.3f, buffer.getFloat());
		assertEquals(1,    buffer.getFloat());
	}

	@DisplayName("A colour can be converted to an RGBA array")
	@Test
	void toArray() {
		final float[] expected = {0.1f, 0.2f, 0.3f, 1};
		assertArrayEquals(expected, colour.toArray());
	}

	@DisplayName("A colour can be parsed from a delimited string")
	@Test
	void parse() {
		assertEquals(colour, Colour.parse("0.1 0.2 0.3 1"));
		assertEquals(colour, Colour.parse("0.1, 0.2, 0.3, 1"));
	}

	@Test
	void equals() {
		assertEquals(colour, colour);
		assertEquals(colour, new Colour(0.1f, 0.2f, 0.3f, 1f));
		assertNotEquals(colour, null);
		assertNotEquals(colour, Colour.WHITE);
	}
}
