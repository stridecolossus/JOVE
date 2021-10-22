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
import org.sarge.jove.common.Vertex;
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
		loader.write(model, out);

		// Re-load and check header
		final Model result = read();
		assertEquals(model.header(), result.header());
		assertEquals(true, result.isIndexed());
	}

	@Test
	void loadUnsupportedVersion() throws IOException {
		new DataOutputStream(out).writeInt(2);
		assertThrows(UnsupportedOperationException.class, () -> read());
	}

	@Test
	void write() throws IOException {
		loader.write(model, out);
	}
}
