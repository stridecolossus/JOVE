package org.sarge.jove.platform.obj;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

class ObjectModelLoaderTest {
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
				s ignored

				v 1 2 3
				v 4 5 6
				v 7 8 9

				vn 1 2 3
				vn 4 5 6
				vn 7 8 9

				vt 1 2
				vt 3 4
				vt 5 6

				s object
				f 1/1/1 2/2/2 3/3/3
		""";

		// Load OBJ models
		final List<IndexedMesh> models = loader.load(new StringReader(data));
		assertEquals(1, models.size());

		// Check model
		final var mesh = models.get(0);

		// Check header
		final var layout = List.of(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT);
		assertEquals(Primitive.TRIANGLE, mesh.primitive());
		assertEquals(3, mesh.count());
		assertEquals(layout, mesh.layout());

		// Check model data
		assertEquals(3 * (3 + 3 + 2) * 4, mesh.vertices().length());
		assertEquals(3 * 4, mesh.index().get().length());
	}

	@Test
	void ignore() throws IOException {
		loader.load(new StringReader("unknown"));
	}

	@Test
	void unknown() throws IOException {
		final var unknown = new ArrayList<String>();
		loader.setUnknownCommandHandler(unknown::add);
		loader.load(new StringReader("unknown"));
		assertEquals(List.of("unknown"), unknown);
	}
}
