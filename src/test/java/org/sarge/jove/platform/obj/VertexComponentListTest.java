package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

public class VertexComponentListTest {
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
	void getNegative() {
		assertEquals(Point.ORIGIN, list.get(-1));
	}

	@Test
	void getInvalidIndex() {
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(2));
		assertThrows(IndexOutOfBoundsException.class, () -> list.get(0));
	}
}
