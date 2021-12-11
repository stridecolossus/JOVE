package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

class ModelBuilderTest {
	private ModelBuilder builder;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = mock(Vertex.class);
		builder = new ModelBuilder();
	}

	@Test
	void build() {
		final Model model = builder
				.primitive(Primitive.TRIANGLES)
				.layout(Point.LAYOUT)
				.add(vertex)
				.add(vertex)
				.add(vertex)
				.build();

		assertNotNull(model);
		assertEquals(Primitive.TRIANGLES, model.primitive());
		assertEquals(List.of(Point.LAYOUT), model.layout());
		assertEquals(3, model.count());
		assertEquals(false, model.isIndexed());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
		assertEquals(false, builder.isEmpty());
	}

	@Test
	void buildIndexed() {
		final Model model = builder
				.layout(Point.LAYOUT)
				.add(vertex)
				.add(0)
				.add(0)
				.add(0)
				.build();

		assertNotNull(model);
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(Point.LAYOUT), model.layout());
		assertEquals(3, model.count());
		assertEquals(true, model.isIndexed());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
		assertEquals(false, builder.isEmpty());
	}

	@Test
	void buildEmpty() {
		final Model model = builder.build();
		assertNotNull(model);
		assertEquals(Primitive.TRIANGLE_STRIP, model.primitive());
		assertEquals(List.of(), model.layout());
		assertEquals(0, model.count());
		assertEquals(false, model.isIndexed());
		assertNotNull(model.vertices());
		assertNotNull(model.index());
		assertEquals(true, builder.isEmpty());
	}

	@Test
	void buildInvalidVertexCount() {
		assertThrows(IllegalArgumentException.class, () -> builder.add(vertex).build());
	}

// TODO - FFS
//	@Test
//	void buildInvalidNormals() {
//		builder.primitive(Primitive.LINE_STRIP);
//		builder.layout(Component.NORMAL);
//		assertThrows(IllegalArgumentException.class, () -> builder.build());
//	}

	@Test
	void addIndexInvalid() {
		assertThrows(IllegalArgumentException.class, () -> builder.add(0));
	}
}
