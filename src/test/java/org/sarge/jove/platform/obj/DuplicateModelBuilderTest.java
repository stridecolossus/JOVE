package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;

class DuplicateModelBuilderTest {
	private DuplicateModelBuilder builder;

	@BeforeEach
	void before() {
		builder = new DuplicateModelBuilder();
	}

	@Test
	void build() {
		// Create some vertices
		final Vertex vertex = Vertex.of(Point.ORIGIN);
		final Vertex other = Vertex.of(new Point(1, 2, 3));

		// Build an indexed model that re-uses some vertices
		final Model model = builder
				.layout(Point.LAYOUT)
				.add(vertex)
				.add(other)
				.add(vertex)
				.build();

		// Verify the de-duplicated model
		assertNotNull(model);
		assertEquals(3, model.header().count());
		assertEquals(2 * Point.LAYOUT.length(), model.vertices().length());
		assertEquals(3 * Short.BYTES, model.index().get().length());
	}
}
