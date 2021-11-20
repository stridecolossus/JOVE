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
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;

class ModelLoaderTest {
	private ModelLoader loader;
	private Model model;
	private ByteArrayOutputStream out;

	@BeforeEach
	void before() {
		// Create a model to persist
		final Model.Header header = new Header(List.of(Point.LAYOUT), Primitive.TRIANGLES, 3);
		final Vertex vertex = Vertex.of(Point.ORIGIN);
		model = new DefaultModel(header, List.of(vertex), new int[]{0, 0, 0});

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
		final Header header = result.header();
		assertNotNull(header);
		//assertEquals(CompoundLayout.of(Point.LAYOUT), header.layout());
		assertEquals(Primitive.TRIANGLES, header.primitive());
		assertEquals(3, header.count());

		// Check layout
		final List<Layout> layouts = header.layout();
		assertNotNull(layouts);
		assertEquals(1, layouts.size());

		final Layout layout = layouts.get(0);
		assertEquals(3, layout.size());
		assertEquals(Float.class, layout.type());
		assertEquals(Float.BYTES, layout.bytes());
		assertEquals(true, layout.signed());
		assertEquals(3 * Float.BYTES, layout.length());

		// Check vertices
		assertNotNull(result.vertices());
		assertEquals(3 * Float.BYTES, result.vertices().length());

		// Check index
		assertEquals(true, result.isIndexed());
		assertNotNull(result.index());
		assertEquals(true, result.index().isPresent());
		assertEquals(3 * Integer.BYTES, result.index().get().length());
	}

	@Test
	void loadUnsupportedVersion() throws IOException {
		new DataOutputStream(out).writeInt(2);
		assertThrows(UnsupportedOperationException.class, () -> read());
	}
}
