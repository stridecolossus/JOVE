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

	@DisplayName("A single quad should be comprised of two triangles")
	@Test
	void quad() {
		final int[] expected = {
				0, 2, 1,
				2, 3, 1
		};
		compare(expected, Triangle.TRIANGLES.strip(1));
	}

	@DisplayName("Each quad in a strip should be comprised of two triangles with the same winding order")
	@Test
	void triangles() {
		final int[] expected = {
				0, 3, 1,
				3, 4, 1,
				1, 4, 2,
				4, 5, 2,
		};
		compare(expected, Triangle.TRIANGLES.strip(2));
	}

	@DisplayName("A strip implemented using a triangle strip should be comprised of alternating indices across the strip")
	@Test
	void strip() {
		final int[] expected = {0, 3, 1, 4, 2, 5};
		compare(expected, Triangle.STRIP.strip(2));
	}
}
