package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.model.BufferedModel.ModelLoader;
import org.sarge.jove.model.Model.IndexedBuilder;

public class BufferedModelTest {
	@Nested
	class BufferedModelTests {
		@Test
		void constructor() {
			final ByteBuffer vertices = ByteBuffer.allocate(1);
			final ByteBuffer index = ByteBuffer.allocate(1);
			final Model model = new BufferedModel(Primitive.LINES, new Vertex.Layout(Vertex.Component.POSITION), vertices, index, 2);
			assertEquals(Primitive.LINES, model.primitive());
			assertEquals(new Vertex.Layout(Vertex.Component.POSITION), model.layout());
			assertEquals(2, model.count());
			assertEquals(vertices, model.vertices());
			assertEquals(Optional.of(index), model.index());
		}
	}

	@Nested
	class ModelLoaderTests {
		private ModelLoader loader;
		private Model model;

		@BeforeEach
		void before() {
			// Create vertex
			final Vertex vertex = new Vertex.Builder().position(new Point(1, 2, 3)).coords(Coordinate2D.BOTTOM_LEFT).build();

			// Create model
			model = new IndexedBuilder()
					.primitive(Primitive.LINES)
					.layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE)
					.add(vertex)
					.add(vertex)
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
			final Model result = loader.load(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
			assertEquals(model, result);
		}
	}
}
