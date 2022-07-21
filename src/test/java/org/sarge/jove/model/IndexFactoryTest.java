package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.IntStream;

import org.junit.jupiter.api.*;

@SuppressWarnings("static-method")
public class IndexFactoryTest {
	@DisplayName("A quad is comprised of a pair of triangles with counter-clockwise winding order")
	@Test
	void pair() {
		assertArrayEquals(new int[]{0, 3, 1, 3, 4, 1}, IndexFactory.triangles(0, 2).toArray());
	}

	@DisplayName("A quad has counter-clockwise indices")
	@Test
	void quad() {
		assertArrayEquals(new int[]{0, 3, 4, 1}, IndexFactory.quad(0, 2).toArray());
	}

	@DisplayName("A quad strip of triangles has two triangles per quad")
	@Test
	void triangles() {
		assertArrayEquals(new int[]{0, 3, 1, 3, 4, 1, 1, 4, 2, 4, 5, 2}, IndexFactory.TRIANGLES.indices(2).toArray());
	}

	@DisplayName("A quad comprising a triangle strip has alternating indices")
	@Test
	void strip() {
		assertArrayEquals(new int[]{0, 3, 1, 4, 2, 5}, IndexFactory.TRIANGLE_STRIP.indices(2).toArray());
	}

	@Test
	void quads() {
		assertArrayEquals(new int[]{0, 3, 4, 1, 1, 4, 5, 2}, IndexFactory.QUADS.indices(2).toArray());
	}

	@DisplayName("A quad strip row offsets the start of each strip")
	@Test
	void row() {
		final IndexFactory factory = width -> IntStream.of(3);
		assertNotNull(factory.row(0, 0));
		assertArrayEquals(new int[]{1 * 3 + 2}, factory.row(1, 2).toArray());
	}
}
