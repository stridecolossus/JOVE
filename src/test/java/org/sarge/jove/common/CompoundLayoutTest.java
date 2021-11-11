package org.sarge.jove.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CompoundLayoutTest {
	private CompoundLayout compound;
	private Layout layout;

	@BeforeEach
	void before() {
		layout = Layout.of(3);
		compound = new CompoundLayout(List.of(layout, layout));
	}

	@Test
	void constructor() {
		assertEquals(List.of(layout, layout), compound.layouts());
	}

	@Test
	void stride() {
		assertEquals(2 * 3 * Float.BYTES, compound.stride());
	}

	@Test
	void contains() {
		assertEquals(true, compound.contains(layout));
		assertEquals(false, compound.contains(Layout.of(3)));
	}

	@Test
	void map() {
		final CompoundLayout that = new CompoundLayout(List.of(layout));
		assertArrayEquals(new int[]{0}, that.map(compound));
	}

	@Test
	void mapSelf() {
		assertArrayEquals(new int[]{0, 0}, compound.map(compound));
	}

	@Test
	void mapInvalidLayout() {
		final CompoundLayout that = new CompoundLayout(List.of(Layout.of(3)));
		assertThrows(IllegalArgumentException.class, () -> compound.map(that));
	}

	@Test
	void add() {
		compound = new CompoundLayout();
		compound.add(layout);
		assertEquals(List.of(layout), compound.layouts());
	}

	@Test
	void equals() {
		assertEquals(true, compound.equals(compound));
		assertEquals(true, compound.equals(new CompoundLayout(List.of(layout, layout))));
		assertEquals(false, compound.equals(null));
		assertEquals(false, compound.equals(new CompoundLayout(List.of(layout))));
	}
}
