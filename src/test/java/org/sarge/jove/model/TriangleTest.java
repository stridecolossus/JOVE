package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class TriangleTest {
	private static void compare(int[] expected, IntStream actual) {
		assertNotNull(actual);
		assertEquals(Arrays.toString(expected), Arrays.toString(actual.toArray()));
	}

	@Test
	void constructor() {
		assertNotNull(Triangle.TRIANGLES);
		assertNotNull(Triangle.STRIP);
	}

	@DisplayName("A triangle should have 012 indices")
	@Test
	void indices() {
		compare(new int[]{0, 1, 2}, Triangle.indices(false));
	}

	@DisplayName("A clockwise triangle should have 021 indices")
	@Test
	void clockwise() {
		compare(new int[]{0, 2, 1}, Triangle.indices(true));
	}

	@DisplayName("A list of triangles should be have alternating winding orders")
	@Test
	void triangles() {
		final int[] expected = {
				0, 1, 2,
				1, 3, 2
		};
		compare(expected, Triangle.TRIANGLES.index(2, false));
	}

	@DisplayName("A clockwise list of triangles should be the opposite orders")
	@Test
	void trianglesClockwise() {
		final int[] expected = {
				0, 2, 1,
				1, 2, 3
		};
		compare(expected, Triangle.TRIANGLES.index(2, true));
	}

	@DisplayName("A triangle strip should share the last two indices of the previous triangle")
	@Test
	void strip() {
		final int[] expected = {
				0, 1, 2,
				1, 2, 3,
				2, 3, 4
		};
		compare(expected, Triangle.STRIP.index(3, false));
	}
}
