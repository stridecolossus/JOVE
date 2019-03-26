package org.sarge.jove.model;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Vertex.MutableVertex;

public class IndexedModelTest {
	private IndexedModel model;
	private MutableVertex a, b;

	@BeforeEach
	public void before() {
		a = new MutableVertex();
		b = new MutableVertex();
		final Model.Builder<MutableVertex> builder = new Model.Builder<>()
			.primitive(Primitive.LINE_STRIP)
			.add(a)
			.add(b);
		model = new IndexedModel(builder.build(), new int[]{1, 0, 1});
	}

	@Test
	public void constructor() {
		assertEquals(3, model.length());
		assertEquals(2, model.vertices().size());
		assertEquals(true, model.isIndexed());
		assertArrayEquals(new int[]{1, 0, 1}, model.indices().toArray());
	}

	@Test
	public void constructorInvalidIndices() {
		final Model.Builder<MutableVertex> builder = new Model.Builder<>()
			.primitive(Primitive.LINE_STRIP)
			.add(a)
			.add(b);
		assertThrows(IllegalArgumentException.class, () -> new IndexedModel(builder.build(), new int[]{1}));
	}

	@Test
	public void faces() {
		final var faces = model.faces();
		assertNotNull(faces);
		assertEquals(List.of(b, a), faces.next());
		assertEquals(List.of(a, b), faces.next());
		assertEquals(false, faces.hasNext());
	}
}
