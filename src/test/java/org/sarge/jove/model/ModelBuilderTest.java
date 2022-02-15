package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;

class ModelBuilderTest {
	private ModelBuilder builder;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex().position(Point.ORIGIN).colour(Colour.WHITE);
		builder = new ModelBuilder();
	}

	@Test
	void build() {
		// Construct a model
		final DefaultModel model = builder
				.primitive(Primitive.LINES)
				.layout(Point.LAYOUT)
				.layout(Colour.LAYOUT)
				.add(vertex)
				.add(vertex)
				.build();

		// Check model header
		assertNotNull(model);
		assertEquals(Primitive.LINES, model.primitive());
		assertEquals(List.of(Point.LAYOUT, Colour.LAYOUT), model.layout());
		assertEquals(2, model.count());

		// Check vertices
		assertNotNull(model.vertices());
		assertArrayEquals(new Vertex[]{vertex, vertex}, model.vertices().toArray());

		// Check index
		assertNotNull(model.index());
		assertEquals(0, model.index().count());

		// Check vertex buffer
		final Bufferable vbo = model.vertexBuffer();
		assertNotNull(vbo);
		assertEquals(2 * (3 + 4) * Float.BYTES, vbo.length());

		// Check empty index buffer
		assertEquals(Optional.empty(), model.indexBuffer());
	}

	@Test
	void indexed() {
		// Construct an indexed model
		final DefaultModel model = builder
				.primitive(Primitive.LINE_STRIP)
				.layout(Point.LAYOUT)
				.layout(Colour.LAYOUT)
				.add(vertex)
				.add(0)
				.add(0)
				.add(0)
				.build();

		// Check model header
		assertNotNull(model);
		assertEquals(3, model.count());

		// Check model
		assertNotNull(model.vertices());
		assertNotNull(model.index());
		assertArrayEquals(new Vertex[]{vertex}, model.vertices().toArray());
		assertArrayEquals(new int[]{0, 0, 0}, model.index().toArray());

		// Check index buffer
		final Optional<Bufferable> index = model.indexBuffer();
		assertNotNull(index);
		assertTrue(index.isPresent());
		assertEquals(2 * 3, index.get().length());
	}

	@Test
	void buildEmpty() {
		final DefaultModel model = builder.build();
		assertNotNull(model);
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(), model.layout());
		assertEquals(0, model.count());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
	}

	@Test
	void addInvalidIndex() {
		assertThrows(IllegalArgumentException.class, () -> builder.add(0));
	}

	@Test
	void restart() {
		final DefaultModel model = builder.restart().build();
		assertNotNull(model);
		assertNotNull(model.index());
		assertArrayEquals(new int[]{-1}, model.index().toArray());
	}
}
