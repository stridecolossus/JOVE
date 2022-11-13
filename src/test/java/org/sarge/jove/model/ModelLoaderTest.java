package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.io.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;

class ModelLoaderTest {
	private ModelLoader loader;
	private ByteArrayOutputStream out;

	@BeforeEach
	void before() {
		out = new ByteArrayOutputStream();
		loader = new ModelLoader();
	}

	@SuppressWarnings("resource")
	@Test
	void map() throws IOException {
		assertNotNull(loader.map(mock(InputStream.class)));
	}

	private BufferedModel read() throws IOException {
		return loader.load(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
	}

	@Test
	void load() throws IOException {
		// Create an indexed model to persist
		final var builder = new IndexedModel(Primitive.TRIANGLES, new Layout(Point.LAYOUT));
		builder.add(Point.ORIGIN);
		builder.add(0);
		builder.add(0);
		builder.add(0);

		// Write model
		loader.save(builder, new DataOutputStream(out));

		// Reload and check is same
		final BufferedModel model = read();
		assertEquals(builder, model);

		// Check vertices
		final ByteSizedBufferable vertices = model.vertices();
		assertEquals(3 * Float.BYTES, vertices.length());

		// Check index
		final ByteSizedBufferable index = model.index().orElseThrow();
		assertEquals(3 * Short.BYTES, index.length());
	}

	@Test
	void loadUnsupportedVersion() throws IOException {
		new DataOutputStream(out).writeInt(2);
		assertThrows(UnsupportedOperationException.class, () -> read());
	}
}
