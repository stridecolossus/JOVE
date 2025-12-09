package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

class VertexComponentListTest {
	private VertexComponentList<Point> list;

	@BeforeEach
	void before() {
		list = new VertexComponentList<>();
		list.add(Point.ORIGIN);
	}

	@Test
	void get() {
		assertEquals(Point.ORIGIN, list.get(1));
	}

	@Test
	void negative() {
		assertEquals(Point.ORIGIN, list.get(-1));
	}

	@Test
	void zero() {
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
	}

	@Test
	void range() {
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(2));
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(-2));
	}
}
