package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout.*;

public class LayoutTest {
	private Layout layout;

	@BeforeEach
	void before() {
		layout = new Layout(3, Type.FLOAT, false, Float.BYTES);
	}

	@Test
	void constructor() {
		assertEquals(3, layout.size());
		assertEquals(Type.FLOAT, layout.type());
		assertEquals(false, layout.signed());
		assertEquals(Float.BYTES, layout.bytes());
	}

	@DisplayName("A layout has a length in bytes")
	@Test
	void length() {
		assertEquals(3 * Float.BYTES, layout.length());
	}

	@DisplayName("A layout has a human-readable representation")
	@Test
	void string() {
		assertEquals("3-FLOAT4U", layout.toString());
	}

	@DisplayName("A floating-point layout can be conveniently created")
	@Test
	void floats() {
		assertEquals(new Layout(3, Type.FLOAT, true, Float.BYTES), Layout.floats(3));
	}

	@Test
	void equals() {
		assertEquals(layout, layout);
		assertEquals(layout, new Layout(3, Type.FLOAT, false, Float.BYTES));
		assertNotEquals(layout, null);
		assertNotEquals(layout, new Layout(3, Type.NORMALIZED, true, 1));
	}

	@Nested
	class CompoundLayoutTests {
		private CompoundLayout compound;

		@BeforeEach
		void before() {
			compound = CompoundLayout.of(layout, layout);
			assertNotNull(compound);
		}

		@DisplayName("A compound layout has a total stride equal to its total length")
		@Test
		void stride() {
			assertEquals(2 * layout.length(), compound.stride());
		}
	}
}
