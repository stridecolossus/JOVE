package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class NormalTest {
	private Normal normal;

	@BeforeEach
	void before() {
		normal = new Normal(new Vector(1, 0, 0));
	}

	@Test
	void constructor() {
		assertEquals(new Vector(1, 0, 0), normal);
	}

	@Test
	void magnitude() {
		assertEquals(1, normal.magnitude());
	}

	@Test
	void normalize() {
		assertSame(normal, normal.normalize());
	}

	@Test
	void invert() {
		assertEquals(new Vector(-1, 0, 0), normal.invert());
	}

	@Test
	void of() {
		assertEquals(normal, Normal.of(new Vector(3, 0, 0)));
	}
}
