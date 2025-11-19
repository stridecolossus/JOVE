package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.geometry.Axis.*;
import static org.sarge.jove.geometry.Point.ORIGIN;

import java.util.List;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.sarge.jove.geometry.Ray.Intersection;

class TriangleTest {
	private Triangle triangle;

	@BeforeEach
	void before() {
		triangle = new Triangle(ORIGIN, new Point(3, 3, 0), new Point(3, 0, 0));
	}

	@Test
	void centre() {
		assertEquals(new Point(2, 1, 0), triangle.centre());
	}

	@Test
	void normal() {
		assertEquals(Z.invert(), triangle.normal());
	}

	@Test
	void isDegenerate() {
		assertEquals(false, triangle.isDegenerate());
		assertEquals(true, new Triangle(ORIGIN, ORIGIN, ORIGIN).isDegenerate());
	}

	@Test
	void winding() {
		assertEquals(WindingOrder.COUNTER_CLOCKWISE, triangle.winding(Z.invert()));
		assertEquals(WindingOrder.CLOCKWISE, triangle.winding(Z));
		assertEquals(WindingOrder.COLINEAR, triangle.winding(X));
	}

	@DisplayName("A ray...")
	@Nested
	class IntersectionTest {
		@DisplayName("intersects if the point is within the triangle")
    	@Test
    	void inside() {
    		final Ray ray = new Ray(triangle.centre(), Z);
    		assertEquals(List.of(new Intersection(0, triangle)), triangle.intersections(ray));
    		assertEquals(Z.invert(), triangle.normal(null));
    	}

		private static List<Point> edge() {
			return List.of(ORIGIN, new Point(3, 3, 0), new Point(3, 0, 0));
		}

		@DisplayName("is considered to be intersecting if the point is on one of the edges of the triangle")
    	@ParameterizedTest
    	@MethodSource
    	void edge(Point p) {
    		final Ray ray = new Ray(p, Z);
    		assertEquals(List.of(new Intersection(0, triangle)), triangle.intersections(ray));
    	}

		@DisplayName("does not intersect if the ray is parallel to the triangle")
    	@Test
    	void parallel() {
    		assertEquals(false, triangle.intersections(new Ray(ORIGIN, X)).iterator().hasNext());
    		assertEquals(false, triangle.intersections(new Ray(ORIGIN, Y)).iterator().hasNext());
    	}

		@DisplayName("does not intersect if the ray is outside the triangle")
    	@Test
    	void outside() {
    		final Ray ray = new Ray(new Point(0, 2, 0), Z);
    		final var intersections = triangle.intersections(ray).iterator();
    		assertEquals(false, intersections.hasNext());
    	}

		@DisplayName("does not intersect if the ray points in the opposite direction to the triangle")
    	@Test
    	void opposite() {
    		final Ray ray = new Ray(new Point(0, 2, 0), Z.invert());
    		final var intersections = triangle.intersections(ray).iterator();
    		assertEquals(false, intersections.hasNext());
    	}
    }
}
