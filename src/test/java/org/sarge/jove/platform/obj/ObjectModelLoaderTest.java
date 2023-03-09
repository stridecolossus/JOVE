package org.sarge.jove.platform.obj;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

public class ObjectModelLoaderTest {
	private ObjectModelLoader loader;

	@BeforeEach
	void before() {
		loader = new ObjectModelLoader();
	}

	@Test
	void load() throws IOException {
		// Create an OBJ file
		final String data = """
				# comment
				o empty

				v 1 2 3
				v 4 5 6
				v 7 8 9

				vn 1 2 3
				vn 4 5 6
				vn 7 8 9

				vt 1 2
				vt 3 4
				vt 5 6

				o group
				s ignored
				f 1/1/1 2/2/2 3/3/3
		""";

		// Load OBJ models
		final List<DefaultMesh> models = loader.load(new StringReader(data));
		assertNotNull(models);
		assertEquals(1, models.size());

		// Check model
		final DefaultMesh model = models.get(0);
		assertNotNull(model);

		// Check header
		final var layout = new CompoundLayout(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT);
		assertEquals(Primitive.TRIANGLE, model.primitive());
		assertEquals(3, model.count());
		assertEquals(layout, model.layout());
		assertEquals(true, model.isIndexed());

		// Check model data
		final BufferedMesh buffer = model.buffer();
		assertEquals(3 * (3 + 3 + 2) * Float.BYTES, buffer.vertexBuffer().length());
		assertEquals(3 * Short.BYTES, buffer.indexBuffer().get().length());
	}

	@Test
	void loadUnknownCommand() throws IOException {
		loader.load(new StringReader("cobblers"));
	}

	@Test
	void setUnknownCommandHandler() throws IOException {
		@SuppressWarnings("unchecked")
		final Consumer<String> handler = mock(Consumer.class);
		loader.setUnknownCommandHandler(handler);
		loader.load(new StringReader("cobblers"));
		verify(handler).accept("cobblers");
	}
}
