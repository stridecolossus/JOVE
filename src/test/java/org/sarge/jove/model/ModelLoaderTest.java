package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;

public class ModelLoaderTest {
	private ModelLoader loader;
	private Model model;

	@BeforeEach
	void before() {
		// Create vertex
		final Vertex vertex = new Vertex.Builder().position(new Point(1, 2, 3)).coords(Coordinate2D.BOTTOM_LEFT).build();

		// Create model
		model = new IndexedModel.IndexedBuilder()
				.primitive(Primitive.LINES)
				.layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE)
				.add(vertex)
				.add(0)
				.add(0)
				.build();

		// Create loader
		loader = new ModelLoader();
	}

	@Test
	void load() throws IOException {
		// Write model
		final ByteArrayOutputStream out = new ByteArrayOutputStream();
		loader.write(model, out);

		// Read back and check is same
		final Model result = loader.load(new ByteArrayInputStream(out.toByteArray()));
		assertEquals(model, result);
	}
}
