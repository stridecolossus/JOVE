package org.sarge.jove.platform.obj;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Model.Header;

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
		final List<Model> models = loader.load(new StringReader(data));
		assertNotNull(models);
		assertEquals(1, models.size());

		// Check model
		final Model model = models.get(0);
		assertNotNull(model);

		// Check header
		final var layout = CompoundLayout.of(Point.LAYOUT, Model.NORMALS, Coordinate2D.LAYOUT);
		assertEquals(new Header(Primitive.TRIANGLES, 3, layout), model.header());

		// Check model data
		assertTrue(model.index().isPresent());
		assertEquals(3 * (3 + 3 + 2) * Float.BYTES, model.vertices().length());
		assertEquals(3 * Short.BYTES, model.index().get().length());
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
