package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;
import static org.sarge.jove.geometry.Axis.Z;

import java.util.List;

import org.junit.jupiter.api.*;

public class PolygonTest {
	private Polygon polygon;
	private Point a, b, c;

	@BeforeEach
	void before() {
		a = Point.ORIGIN;
		b = new Point(3, 0, 0);
		c = new Point(3, 3, 0);
		polygon = new Polygon(List.of(a, b, c));
	}

	@DisplayName("A polygon must have at least 3 vertices")
	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> new Polygon(List.of(a, b)));
	}

	@DisplayName("A polygon has three-or-more vertices")
	@Test
	void vertices() {
		assertEquals(List.of(a, b, c), polygon.vertices());
	}

	@DisplayName("The centre point of a polygon is the average of the vertices")
	@Test
	void centre() {
		assertEquals(new Point(2, 1, 0), polygon.centre());
	}

	@DisplayName("A polygon can be converted to a number of edge vectors")
	@Test
	void edges() {
		assertEquals(List.of(new Vector(3, 0, 0), new Vector(0, 3, 0)), polygon.edges().toList());
	}

	@DisplayName("A polygon can be closed by appending the first vertex to the end of the polygon")
	@Test
	void close() {
		final Polygon closed = polygon.close();
		assertEquals(List.of(new Vector(3, 0, 0), new Vector(0, 3, 0), new Vector(-3, -3, 0)), closed.edges().toList());
	}

	@DisplayName("A polygon has a normal")
	@Test
	void normal() {
		assertEquals(Axis.Z, polygon.normal().normalize());
	}

	@DisplayName("A polygon...")
	@Nested
	class WindingTests {
		@DisplayName("with a normal in the same direction as the view axis has a counter-clockwise winding order")
    	@Test
    	void counter() {
    		assertEquals(WindingOrder.COUNTER_CLOCKWISE, polygon.winding(Z));
    	}

		@DisplayName("with a normal opposite to the view axis has a clockwise winding order")
    	@Test
    	void clockwise() {
    		assertEquals(WindingOrder.CLOCKWISE, polygon.winding(Z.invert()));
    	}

		@DisplayName("comprising a line is colinear")
    	@Test
    	void colinear() {
    		polygon = new Polygon(List.of(a, a, a));
    		assertEquals(WindingOrder.COLINEAR, polygon.winding(Z));
    		assertEquals(WindingOrder.COLINEAR, polygon.winding(Z.invert()));
    	}
    }
}
