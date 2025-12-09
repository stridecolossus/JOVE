package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout.Type;

public class LayoutTest {
	private Layout layout;

	@BeforeEach
	void before() {
		layout = new Layout(3, Type.FLOAT, false, Float.BYTES);
	}

	@DisplayName("A layout has a stride in bytes")
	@Test
	void stride() {
		assertEquals(3 * Float.BYTES, layout.stride());
	}

	@DisplayName("A compound layout has a stride")
	@Test
	void compound() {
		assertEquals(2 * 3 * Float.BYTES, Layout.stride(List.of(layout, layout)));
	}

	@DisplayName("A layout has a human-readable representation")
	@Test
	void string() {
		assertEquals("3-FLOAT4U", layout.toString());
	}

	@DisplayName("A signed floating-point layout can be conveniently created")
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
}
