package org.sarge.jove.platform.obj;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Model.Header;
import org.sarge.jove.model.Primitive;

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

				v 1 2 3
				v 4 5 6
				v 7 8 9

				vn 1 2 3
				vn 4 5 6
				vn 7 8 9

				vt 1 2
				vt 3 4
				vt 5 6

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
		final Header header = model.header();
		assertEquals(3, header.count());
		assertEquals(Primitive.TRIANGLES, header.primitive());
		assertEquals(List.of(Point.LAYOUT, Vector.NORMALS, Coordinate2D.LAYOUT), header.layout());

		// Check vertex buffer
		assertNotNull(model.vertices());
		// TODO
//			assertEquals(3 * (3 + 3 + 2) * Float.BYTES, model.vertices().limit());
		// TODO - check texture flip

		// Check index buffer
		assertNotNull(model.index());
		assertEquals(true, model.index().isPresent());
//			assertEquals(3 * Integer.BYTES, model.indexBuffer().get().limit());
	}

	@Test
	void loadUnknownCommand() throws IOException {
		loader.load(new StringReader("cobblers"));
	}

	@Test
	void setUnknownCommandHandler() throws IOException {
		final Consumer<String> handler = mock(Consumer.class);
		loader.setUnknownCommandHandler(handler);
		loader.load(new StringReader("cobblers"));
		verify(handler).accept("cobblers");
	}

	@Test
	void flip() {
		// TODO
//		assertEquals(Coordinate.of(new float[]{1, -2, 3}), ObjectModelLoader.FLIP.apply(new float[]{1, 2, 3}));
	}
}
