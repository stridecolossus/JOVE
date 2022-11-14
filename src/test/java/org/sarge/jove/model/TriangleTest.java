package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;

public class TriangleTest {
	private Triangle triangle;

	@BeforeEach
	void before() {
		triangle = new Triangle(Point.ORIGIN, new Point(3, 0, 0), new Point(3, 3, 0));
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new Triangle(List.of()));
	}

	@Test
	void normal() {
		assertEquals(Axis.Z, triangle.normal());
	}

	@Test
	void isDegenerate() {
		assertEquals(false, triangle.isDegenerate());
		assertEquals(true, new Triangle(Collections.nCopies(3, Point.ORIGIN)).isDegenerate());
	}
}
