package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout.Type;

public class LayoutTest {
	private Layout layout;

	@BeforeEach
	void before() {
		layout = new Layout(3, Type.FLOAT, Float.BYTES, true);
	}

	@Test
	void constructor() {
		assertEquals(3, layout.size());
		assertEquals(Type.FLOAT, layout.type());
		assertEquals(Float.BYTES, layout.bytes());
		assertEquals(true, layout.signed());
		assertEquals(3 * Float.BYTES, layout.length());
		assertEquals("3-FLOAT4", layout.toString());
	}

	@Test
	void floats() {
		assertEquals(layout, Layout.floats(3));
	}

	@Test
	void stride() {
		assertEquals(2 * layout.length(), Layout.stride(List.of(layout, layout)));
	}

	@Test
	void equals() {
		assertEquals(layout, layout);
		assertEquals(layout, new Layout(3, Type.FLOAT, Float.BYTES, true));
		assertNotEquals(layout, null);
		assertNotEquals(layout, new Layout(3, Type.NORMALIZED, Float.BYTES, true));
	}
}
