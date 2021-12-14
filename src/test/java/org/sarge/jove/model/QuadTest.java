package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;

public class QuadTest {
	@Test
	void coordinates() {
		assertEquals(List.of(Coordinate2D.TOP_LEFT, Coordinate2D.BOTTOM_LEFT, Coordinate2D.TOP_RIGHT, Coordinate2D.BOTTOM_RIGHT), Quad.COORDINATES);
	}

	@Test
	void constructor() {
		assertNotNull(Quad.STRIP);
	}

	private static void compare(int[] expected, IntStream actual) {
		assertNotNull(actual);
		assertEquals(Arrays.toString(expected), Arrays.toString(actual.toArray()));
	}

	@DisplayName("Quad indices should be counter-clockwise by default")
	@Test
	void indices() {
		compare(new int[]{0, 2, 3, 1}, Quad.STRIP.strip(1));
	}

	@DisplayName("A strip of quads should share the last two indices of each quad")
	@Test
	void strip() {
		final int[] expected = {
				0, 4, 5, 1,
				1, 5, 6, 2,
				2, 6, 7, 3,
		};
		compare(expected, Quad.STRIP.strip(3));
	}
}
