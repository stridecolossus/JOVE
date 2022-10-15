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

	private Mesh read() throws IOException {
		return loader.load(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
	}

	@Test
	void load() throws IOException {
		// Create an indexed model to persist
		final Model model = new Model(Primitive.TRIANGLES)
				.layout(Point.LAYOUT)
				.add(Vertex.of(Point.ORIGIN))
				.add(0)
				.add(0)
				.add(0);

		// Write model to stream
		loader.save(model, new DataOutputStream(out));

		// Re-load and check header
		final Mesh mesh = read();
		assertNotNull(mesh);

		// Check header
		final Header header = mesh.header();
		assertEquals(Primitive.TRIANGLES, header.primitive());
		assertEquals(3, header.count());
		assertEquals(new Layout(Point.LAYOUT), header.layout());
		assertEquals(true, header.isIndexed());

		// Check vertices
		final Bufferable vertices = mesh.vertices();
		assertNotNull(vertices);
		assertEquals(3 * Float.BYTES, vertices.length());

		// Check index
		final Optional<Bufferable> index = mesh.index();
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
