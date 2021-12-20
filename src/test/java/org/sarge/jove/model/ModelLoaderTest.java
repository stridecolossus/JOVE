package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;

class ModelLoaderTest {
	private ModelLoader loader;
	private Model model;
	private ByteArrayOutputStream out;

	@BeforeEach
	void before() {
		// Create a model to persist
		model = new MutableModel(Primitive.TRIANGLES, List.of(Point.LAYOUT))
				.add(new Vertex(Point.ORIGIN))
				.add(0)
				.add(0)
				.add(0);

		// Init persistence store
		out = new ByteArrayOutputStream();

		// Create loader
		loader = new ModelLoader();
	}

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
		assertNotNull(result.vertexBuffer());
		assertEquals(3 * Float.BYTES, result.vertexBuffer().length());

		// Check index
		assertNotNull(result.indexBuffer());
		assertEquals(true, result.isIndexed());
		assertEquals(3 * Integer.BYTES, result.indexBuffer().length());
	}

	@Test
	void loadUnsupportedVersion() throws IOException {
		new DataOutputStream(out).writeInt(2);
		assertThrows(UnsupportedOperationException.class, () -> read());
	}
}
