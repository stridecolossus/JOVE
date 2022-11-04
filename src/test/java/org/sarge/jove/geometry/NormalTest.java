package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class NormalTest {
	private Normal normal;

	@BeforeEach
	void before() {
		normal = new Normal(new Vector(1, 0, 0));
	}

	@DisplayName("A normalized vector is a unit-vector")
	@Test
	void magnitude() {
		assertEquals(new Vector(1, 0, 0), normal);
		assertEquals(1, normal.magnitude());
	}

	@DisplayName("A normalized vector is unchanged if it is re-normalised")
	@Test
	void normalize() {
		assertSame(normal, normal.normalize());
	}

	@DisplayName("A normalized vector can be inverted")
	@Test
	void invert() {
		assertEquals(new Vector(-1, 0, 0), normal.invert());
	}

	@DisplayName("A normalized vector can be created from a non-unit vector")
	@Test
	void unit() {
		assertEquals(normal, new Normal(new Vector(3, 0, 0)));
	}

	@DisplayName("A normalized vector can be copied")
	@Test
	void copy() {
		assertEquals(normal, new Normal(normal));
	}
}
