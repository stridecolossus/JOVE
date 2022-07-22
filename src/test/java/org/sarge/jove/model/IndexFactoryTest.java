package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

@SuppressWarnings("static-method")
public class IndexFactoryTest {
	@DisplayName("A strip comprised of triangles has two counter-clockwise triangles per quad")
	@Test
	void triangles() {
		assertArrayEquals(new int[]{0, 2, 1, 2, 3, 1}, IndexFactory.TRIANGLES.indices(1).toArray());
		assertArrayEquals(new int[]{0, 3, 1, 3, 4, 1, 1, 4, 2, 4, 5, 2}, IndexFactory.TRIANGLES.indices(2).toArray());
	}

	@DisplayName("A quad comprising a triangle-strip has alternating indices")
	@Test
	void strip() {
		assertArrayEquals(new int[]{0, 2, 1, 3}, IndexFactory.TRIANGLE_STRIP.indices(1).toArray());
		assertArrayEquals(new int[]{0, 3, 1, 4, 2, 5}, IndexFactory.TRIANGLE_STRIP.indices(2).toArray());
	}

	@DisplayName("A quad strip is comprised of counter-clockwise indices")
	@Test
	void quads() {
		assertArrayEquals(new int[]{0, 2, 3, 1}, IndexFactory.QUADS.indices(1).toArray());
		assertArrayEquals(new int[]{0, 3, 4, 1, 1, 4, 5, 2}, IndexFactory.QUADS.indices(2).toArray());
	}

	@DisplayName("A row adapter generates indices for the row of a grid")
	@Test
	void row() {
		final IndexFactory row = IndexFactory.QUADS.row(1);
		assertNotNull(row);
		assertArrayEquals(new int[]{2, 4, 5, 3}, row.indices(1).toArray());
	}
}
