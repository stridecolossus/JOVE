package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.io.*;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;

class ModelLoaderTest {
	private ModelLoader loader;
	private Model model;
	private ByteArrayOutputStream out;

	@BeforeEach
	void before() {
		// Create a model to persist
		model = new DefaultModel.Builder()
				.primitive(Primitive.TRIANGLES)
				.layout(Point.LAYOUT)
				.add(Vertex.of(Point.ORIGIN))
				.add(0)
				.add(0)
				.add(0)
				.build();

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
		assertEquals(Primitive.TRIANGLES, model.primitive());
		assertEquals(3, model.count());
		assertEquals(new Layout(Point.LAYOUT), result.layout());
		assertEquals(true, result.isIndexed());

		// Check vertices
		final Bufferable vertices = result.vertices();
		assertNotNull(vertices);
		assertEquals(3 * Float.BYTES, vertices.length());

		// Check index
		final Optional<Bufferable> index = result.index();
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
