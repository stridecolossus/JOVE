package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class LayoutTest {
	private Layout layout;

	@BeforeEach
	void before() {
		layout = new Layout(3, Float.class, Float.BYTES, true);
	}

	@Test
	void constructor() {
		assertEquals(3, layout.size());
		assertEquals(Float.class, layout.type());
		assertEquals(Float.BYTES, layout.bytes());
		assertEquals(true, layout.signed());
		assertEquals(3 * Float.BYTES, layout.length());
		assertEquals("3Float4", layout.toString());
	}

	@Test
	void floats() {
		assertEquals(layout, Layout.floats(3));
	}

	@Test
	void bytes() {
		layout = Layout.bytes(3, 2);
		assertEquals(3, layout.size());
		assertEquals(Byte.class, layout.type());
		assertEquals(2, layout.bytes());
		assertEquals(false, layout.signed());
		assertEquals(3 * 2, layout.length());
		assertEquals("3Byte2U", layout.toString());
	}

	@Test
	void stride() {
		assertEquals(2 * 3 * Float.BYTES, Layout.stride(List.of(layout, layout)));
	}

	@Test
	void equals() {
		assertEquals(true, layout.equals(layout));
		assertEquals(true, layout.equals(new Layout(3, Float.class, Float.BYTES, true)));
		assertEquals(false, layout.equals(null));
		assertEquals(false, layout.equals(new Layout(4, Float.class, Float.BYTES, true)));
	}

	@Nested
	class BytesTests {
		@Test
		void floats() {
			assertEquals(Float.BYTES, Layout.bytes(Float.class));
			assertEquals(Float.BYTES, Layout.bytes(float.class));
		}

		@Test
		void integers() {
			assertEquals(Integer.BYTES, Layout.bytes(Integer.class));
			assertEquals(Integer.BYTES, Layout.bytes(int.class));
		}

		@Test
		void shorts() {
			assertEquals(Short.BYTES, Layout.bytes(Short.class));
			assertEquals(Short.BYTES, Layout.bytes(short.class));
		}

		@Test
		void bytes() {
			assertEquals(Byte.BYTES, Layout.bytes(Byte.class));
			assertEquals(Byte.BYTES, Layout.bytes(Byte.class));
		}

		@Test
		void unsupported() {
			assertThrows(IllegalArgumentException.class, () -> Layout.bytes(String.class));
		}
	}
}
