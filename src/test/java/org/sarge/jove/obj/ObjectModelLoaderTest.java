package org.sarge.jove.obj;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringReader;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.model.Model;
import org.sarge.jove.obj.ObjectModel.Face;
import org.sarge.jove.obj.ObjectModel.Group;
import org.sarge.jove.obj.ObjectModelLoader.FaceParser;
import org.sarge.jove.obj.ObjectModelLoader.MaterialLibraryParser;

public class ObjectModelLoaderTest {
	private ObjectModelLoader loader;

	@BeforeEach
	public void before() {
		loader = new ObjectModelLoader();
	}

	@Test
	public void constructor() {
		assertNotNull(loader.library());
	}

	@Test
	public void load() throws IOException {
		// Create OBJ file
		final String[] data = {
			"# comment",
			"g group",
			"usemtl mat",
			"v 1 1 1",
			"v 2 2 2",
			"v 3 3 3",
			"vt 1 1",
			"vt 2 2",
			"vt 3 3",
			"vn 1 1 1",
			"vn 2 2 2",
			"vn 3 3 3",
			"f 1/1/1 2/2/2 3/3/3",
			"f 1/1 2/2 3/3",
			"f 1//1 2//2 3//3",
		};

		// Add a material
		final ObjectMaterial mat = new ObjectMaterial.Builder("mat").build();
		loader.library().add(mat);

		// Load model
		final ObjectModel obj = loader.load(new StringReader(String.join("\n", data)));
		assertNotNull(obj);

		// Check group
		final Group group = obj.group();
		assertEquals("group", group.name());
		assertEquals(Optional.of(mat), group.material());

		// Build generic model
		final Model<?> model = group.build();
		assertEquals(false, model.isIndexed());
		assertEquals(3 * 3, model.vertices().size());
	}

	@Nested
	class ParserTests {
		private ObjectModel model;

		@BeforeEach
		public void before() {
			final Group group = mock(Group.class);
			model = mock(ObjectModel.class);
			when(model.group()).thenReturn(group);
		}

		@Test
		public void group() {
			ObjectModelLoader.GROUP.parse(new String[]{"g", "name"}, model);
			verify(model).group("name");
		}

		@Test
		public void faceTriangle() {
			final var parser = new FaceParser();
			parser.parse(new String[]{"f", "1", "1", "1"}, model);
			verify(model.group(), times(3)).face(new Face(1, 0, 0));
		}

		@Test
		public void faceVertexOnly() {
			final var parser = new FaceParser();
			parser.parse(new String[]{"f", "1"}, model);
			verify(model.group()).face(new Face(1, 0, 0));
		}

		@Test
		public void faceVertexTexture() {
			final var parser = new FaceParser();
			parser.parse(new String[]{"f", "1/2"}, model);
			verify(model.group()).face(new Face(1, 2, 0));
		}

		@Test
		public void faceVertexTextureNormal() {
			final var parser = new FaceParser();
			parser.parse(new String[]{"f", "1/2/3"}, model);
			verify(model.group()).face(new Face(1, 2, 3));
		}

		@Test
		public void faceVertexNormalOptionalTexture() {
			final var parser = new FaceParser();
			parser.parse(new String[]{"f", "1//2"}, model);
			verify(model.group()).face(new Face(1, 0, 2));
		}

		@Test
		public void useMaterial() {
			// Add a material
			final ObjectMaterial mat = mock(ObjectMaterial.class);
			when(mat.name()).thenReturn("name");
			loader.library().add(mat);

			// Use material
			final var parser = loader.new MaterialParser();
			parser.parse(new String[]{"usemtl", "name"}, model);
			verify(model.group()).material(mat);
		}

		@Test
		public void materialLibrary() {
			final MaterialLibraryParser parser = loader.new MaterialLibraryParser();
			parser.parse(new String[]{"mtllib", "filename"}, model);
			// TODO
		}
	}
}
