package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Point;

class ModelLoaderTest {
	private ModelLoader loader;
	private Model model;
	private ByteArrayOutputStream out;

	@BeforeEach
	void before() {
		// Create a model to persist
		model = new MutableModel()
				.primitive(Primitive.TRIANGLES)
				.layout(Point.LAYOUT)
				.add(Vertex.of(Point.ORIGIN))
				.add(0)
				.add(0)
				.add(0);

		// Init persistence store
		out = new ByteArrayOutputStream();

		// Create loader
		loader = new ModelLoader();
	}

	@SuppressWarnings("resource")
	@Test
	void map() throws IOException {
		assertNotNull(loader.map(mock(InputStream.class)));
	}

	private Model read() throws IOException {
		return loader.load(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
	}

	@Test
	void load() throws IOException {
		// Write model to stream
		loader.save(model, new DataOutputStream(out));

		// Re-load and check header
		final Model result = read();
		assertNotNull(result);

		// Check header
		assertEquals(List.of(Point.LAYOUT), result.layout());
		assertEquals(Primitive.TRIANGLES, result.primitive());
		assertEquals(3, result.count());

		// Check vertices
		final Bufferable vertices = result.vertexBuffer();
		assertNotNull(vertices);
		assertEquals(3 * Float.BYTES, vertices.length());

		// Check index
		final Optional<Bufferable> index = result.indexBuffer();
		assertNotNull(index);
		assertTrue(index.isPresent());
		assertEquals(3 * Short.BYTES, index.get().length());
	}

	@Test
	void loadUnsupportedVersion() throws IOException {
		new DataOutputStream(out).writeInt(2);
		assertThrows(UnsupportedOperationException.class, () -> read());
	}
}
