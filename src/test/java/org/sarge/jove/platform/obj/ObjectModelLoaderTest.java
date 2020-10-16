package org.sarge.jove.platform.obj;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.StringReader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Model.IndexedBuilder;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.obj.ObjectModelLoader.ObjectModel;
import org.sarge.jove.platform.obj.ObjectModelLoader.Parser;

public class ObjectModelLoaderTest {
	private ObjectModelLoader loader;

	@BeforeEach
	void before() {
		loader = new ObjectModelLoader();
	}

	@Nested
	class LoaderTests {
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

			// Load OBJ model
			final Model.Builder builder = loader.load(new StringReader(data));
			assertNotNull(builder);

			// Check generated model
			final Model model = builder.build();
			assertEquals(Primitive.TRIANGLES, model.primitive());
			assertEquals(new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.NORMAL, Vertex.Component.TEXTURE_COORDINATE), model.layout());
			assertEquals(3, model.size());

			// Check vertex buffer
			assertNotNull(model.vertices());
			assertEquals(3 * (3 + 3 + 2) * Float.BYTES, model.vertices().length());

			// Check index buffer
			assertNotNull(model.index());
			assertEquals(true, model.index().isPresent());
			assertEquals(3 * Integer.BYTES, model.index().get().length());
		}

		@Test
		void loadUnknownCommand() {
			assertThrows(IOException.class, () -> loader.load(new StringReader("cobblers")));
		}

		@Test
		void loadIgnoreUnknownCommand() throws IOException {
			loader.setUnknownCommandHandler(ObjectModelLoader.HANDLER_IGNORE);
			loader.load(new StringReader("cobblers"));
		}
	}

	@Nested
	class ParserTests {
		@Nested
		class ArrayParserTests {
			private Parser parser;
			private ObjectModel model;

			@BeforeEach
			void before() {
				parser = Parser.of(3, Point::new, ObjectModel::vertex);
				model = mock(ObjectModel.class);
			}

			@Test
			void constructor() {
				assertNotNull(parser);
			}

			@Test
			void array() {
				parser.parse(new String[]{"1", "2", "3"}, model);
				verify(model).vertex(new Point(1, 2, 3));
			}

			@Test
			void arrayInvalidLength() {
				assertThrows(IllegalArgumentException.class, () -> parser.parse(new String[]{}, model));
			}
		}

		@Nested
		class FaceParserTests {
			private ObjectModel model;

			@BeforeEach
			void before() {
				final ObjectModel actual = loader.new ObjectModel(new IndexedBuilder());
				actual.vertex(Point.ORIGIN);
				actual.normal(Vector.X_AXIS);
				actual.coord(Coordinate2D.BOTTOM_LEFT);
				model = spy(actual);
			}

			@Test
			void parsePosition() {
				Parser.FACE.parse(new String[]{"1", "1", "1"}, model);
				verify(model, times(3)).add(Vertex.of(Point.ORIGIN));
				verify(model).update(3);
			}

			@Test
			void parsePositionTexture() {
				final Vertex expected  = new Vertex.Builder().position(Point.ORIGIN).coords(Coordinate2D.BOTTOM_LEFT).build();
				Parser.FACE.parse(new String[]{"1/1", "1/1", "1/1"}, model);
				verify(model, times(3)).add(expected);
				verify(model).update(3);
			}

			@Test
			void parsePositionTextureNormal() {
				final Vertex expected  = new Vertex.Builder().position(Point.ORIGIN).coords(Coordinate2D.BOTTOM_LEFT).normal(Vector.X_AXIS).build();
				Parser.FACE.parse(new String[]{"1/1/1", "1/1/1", "1/1/1"}, model);
				verify(model, times(3)).add(expected);
				verify(model).update(3);
			}

			@Test
			void parseNegativeIndex() {
				Parser.FACE.parse(new String[]{"-1", "-1", "-1"}, model);
				verify(model, times(3)).add(Vertex.of(Point.ORIGIN));
			}

			@Test
			void parseInvalidIndex() {
				assertThrows(IndexOutOfBoundsException.class, () -> Parser.FACE.parse(new String[]{"1", "1", "999"}, model));
				assertThrows(IndexOutOfBoundsException.class, () -> Parser.FACE.parse(new String[]{"1", "1", "0"}, model));
			}

			@Test
			void parseFaceSizeMismatch() {
				Parser.FACE.parse(new String[]{"1"}, model);
				assertThrows(IllegalArgumentException.class, () -> Parser.FACE.parse(new String[]{"1 2"}, model));
			}
		}
	}
}
