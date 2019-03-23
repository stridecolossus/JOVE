package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExtentsTest {
	private Extents extents;
	private Point min, max;

	@BeforeEach
	public void before() {
		min = new Point(1, 2, 3);
		max = new Point(5, 6, 7);
		extents = new Extents(min, max);
	}

	@Test
	public void constructor() {
		assertEquals(min, extents.min());
		assertEquals(max, extents.max());
	}

	@Test
	public void centre() {
		assertEquals(new Point(3, 4, 5), extents.centre());
	}

	@Test
	public void size() {
		assertEquals(4f, extents.size(), 0.0001f);
	}

	@Test
	public void add() {
		final Point pt = new Point(5, 6, 9);
		final Extents other = new Extents(min, pt);
		assertEquals(other, extents.add(other));
	}

	@Test
	public void build() {
		final Extents result = new Extents.Builder()
			.add(min)
			.add(min)
			.add(max)
			.build();
		assertEquals(extents, result);
	}
}
