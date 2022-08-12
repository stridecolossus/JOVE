package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.io.*;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;

class ModelLoaderTest {
	private ModelLoader loader;
	private Model model;
	private ByteArrayOutputStream out;

	@BeforeEach
	void before() {
		// Create a model to persist
		model = new Model.Builder()
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
		assertEquals(new Header(Primitive.TRIANGLES, 3, CompoundLayout.of(Point.LAYOUT)), result.header());

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
