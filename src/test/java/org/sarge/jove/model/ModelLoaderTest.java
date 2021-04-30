package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Model.Header;

class ModelLoaderTest {
	private ModelLoader loader;
	private Model model;
	private ByteArrayOutputStream out;

	@BeforeEach
	void before() {
		// Create a model to persist
		final Model.Header header = new Header(List.of(Point.LAYOUT), Primitive.TRIANGLES, 3, false);
		final Vertex vertex = Vertex.of(Point.ORIGIN);
		model = new DefaultModel(header, List.of(vertex), List.of(0, 0, 0));

		// Init persistence store
		out = new ByteArrayOutputStream();

		// Create loader
		loader = new ModelLoader();
	}

	@Test
	void write() throws IOException {
		loader.write(model, out);
	}

	private Model read() throws IOException {
		return loader.load(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
	}

	@Test
	void load() throws IOException {
		// Persist the model
		write();
		final Model result = read();

		// Check header
		assertEquals(model.header(), result.header());
		assertEquals(true, result.isIndexed());

		// Check buffers
		assertArrayEquals(model.vertexBuffer().toByteArray(), result.vertexBuffer().toByteArray());
		assertArrayEquals(model.indexBuffer().get().toByteArray(), result.indexBuffer().get().toByteArray());
	}

	@Test
	void loadUnsupportedVersion() throws IOException {
		new DataOutputStream(out).writeInt(2);
		assertThrows(UnsupportedOperationException.class, () -> read());
	}
}
