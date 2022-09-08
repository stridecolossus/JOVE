package org.sarge.jove.geometry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

public class NormalizedVectorTest {
	private NormalizedVector vec;

	@BeforeEach
	void before() {
		vec = new NormalizedVector(new Vector(1, 0, 0));
	}

	@Test
	void magnitude() {
		assertEquals(1, vec.magnitude());
	}

	@Test
	void normalize() {
		assertSame(vec, vec.normalize());
	}
}
