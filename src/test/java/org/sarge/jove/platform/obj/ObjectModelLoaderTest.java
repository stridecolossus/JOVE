package org.sarge.jove.platform.obj;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sarge.jove.util.TestHelper.assertThrows;

import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.model.VertexModel;

public class ObjectModelLoaderTest {
	private ObjectModelLoader loader;

	@BeforeEach
	void before() {
		loader = new ObjectModelLoader();
	}

	@Nested
	class LoaderTests {
		private String data;

		@BeforeEach
		void before() {
			// Create an OBJ file
			data = """
					# comment

					v 1 2 3
					v 4 5 6
					v 7 8 9

					vn 1 2 3
					vn 4 5 6
					vn 7 8 9

					vt 1 2
					vt 3 4
					vt 5 6

					g
					f 1/1/1 2/2/2 3/3/3
			""";
		}

		@Test
		void load() throws IOException {
			// Load OBJ models
			final Stream<Model> models = loader.load(new StringReader(data));
			assertNotNull(models);

			// Check number of models generated
			final Model[] array = models.toArray(Model[]::new);
			assertEquals(1, array.length);

			// Check model
			final VertexModel model = (VertexModel) array[0];
			assertNotNull(model);
			assertEquals(Primitive.TRIANGLES, model.header().primitive());
			assertEquals(new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.NORMAL, Vertex.Component.COORDINATE), model.layout());
			assertEquals(3, model.header().count());

			// Check vertex buffer
			assertNotNull(model.vertices());
			// TODO
//			assertEquals(3 * (3 + 3 + 2) * Float.BYTES, model.vertices().limit());
			// TODO - check texture flip

			// Check index buffer
			assertNotNull(model.indexBuffer());
			assertEquals(true, model.indexBuffer().isPresent());
//			assertEquals(3 * Integer.BYTES, model.indexBuffer().get().limit());
		}

		@Test
		void loadUnknownCommand() {
			assertThrows(IOException.class, "Unsupported OBJ command", () -> loader.load(new StringReader("cobblers")));
		}

		@Test
		void loadIgnoreUnknownCommand() throws IOException {
			loader.setUnknownCommandHandler(ObjectModelLoader.HANDLER_IGNORE);
			loader.load(new StringReader("cobblers\n" + data));
		}
	}
}
