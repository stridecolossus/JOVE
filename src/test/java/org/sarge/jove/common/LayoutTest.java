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
		layout = new Layout(3, Float.class, true);
	}

	@Test
	void constructor() {
		assertEquals(3, layout.size());
		assertEquals(Float.class, layout.type());
		assertEquals(Float.BYTES, layout.bytes());
		assertEquals(true, layout.signed());
	}

	@Test
	void length() {
		assertEquals(3 * 4, layout.length());
	}

	@Test
	void of() {
		assertEquals(layout, Layout.of(3));
	}

	@Test
	void stride() {
		final int stride = Layout.stride(List.of(Layout.of(2), Layout.of(3)));
		assertEquals((2 + 3) * 4, stride);
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
