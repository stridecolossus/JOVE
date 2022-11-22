package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;

public class NormalTest {
	private Normal normal;

	@BeforeEach
	void before() {
		normal = new Normal(new Vector(1, 0, 0));
	}

	@DisplayName("A normal is a unit-vector")
	@Test
	void magnitude() {
		assertEquals(new Vector(1, 0, 0), normal);
		assertEquals(1, normal.magnitude());
	}

	@DisplayName("A normal is unchanged if it is normalized")
	@Test
	void normalize() {
		assertSame(normal, normal.normalize());
	}

	@DisplayName("An inverted normal is also a normal")
	@Test
	void invert() {
		assertEquals(new Vector(-1, 0, 0), normal.invert());
	}

	@DisplayName("A normal can be created from an arbitrary vector")
	@Test
	void unit() {
		assertEquals(normal, new Normal(new Vector(3, 0, 0)));
	}

	@DisplayName("A normal can be copied")
	@Test
	void copy() {
		assertEquals(normal, new Normal(normal));
	}

	@DisplayName("A normal has a vertex layout")
	@Test
	void layout() {
		assertEquals(Layout.floats(3), Normal.LAYOUT);
		assertEquals(Point.LAYOUT, normal.layout());
	}
}
