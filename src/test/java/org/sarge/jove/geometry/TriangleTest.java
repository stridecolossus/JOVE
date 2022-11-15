package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;

public class TriangleTest {
	private Triangle triangle;

	@BeforeEach
	void before() {
		triangle = new Triangle(Point.ORIGIN, new Point(3, 0, 0), new Point(3, 3, 0));
	}

	@Test
	void invalid() {
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> new Triangle(List.of()));
		assertThrows(IllegalArgumentException.class, () -> new Triangle(Collections.nCopies(4, Point.ORIGIN)));
	}

	@Test
	void centre() {
		assertEquals(new Point(2, 1, 0), triangle.centre());
	}

	@Test
	void normal() {
		assertEquals(Axis.Z, triangle.normal().normalize());
	}

	@Test
	void winding() {
		assertEquals(WindingOrder.COUNTER_CLOCKWISE, triangle.winding(Axis.Z));
	}

	@Test
	void isDegenerate() {
		assertEquals(false, triangle.isDegenerate());
		assertEquals(true, new Triangle(Collections.nCopies(3, Point.ORIGIN)).isDegenerate());
	}
}
