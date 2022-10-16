package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;

public class ModelTest {
	private Model model;
	private Vertex vertex;

	@BeforeEach
	void before() {
		model = new Model(Primitive.POINTS);
		vertex = Vertex.of(Point.ORIGIN);
	}

	@DisplayName("A new model...")
	@Nested
	class New {
		@DisplayName("has an undefined layout")
		@Test
		void constructor() {
			assertEquals(Primitive.POINTS, model.primitive());
			assertEquals(new Layout(), model.layout());
		}

		@DisplayName("is not indexed")
		@Test
		void unindexed() {
			assertEquals(false, model.isIndexed());
		}

		@DisplayName("is initially empty")
		@Test
		void empty() {
			assertEquals(0, model.count());
			assertEquals(0, model.vertices().count());
			assertEquals(0, model.index().count());
		}

		@DisplayName("can define the vertex layout")
		@Test
		void layout() {
			model.layout(Point.LAYOUT);
			assertEquals(new Layout(Point.LAYOUT), model.layout());
		}

		@DisplayName("cannot define a layout containing normals if they are not supported by the primitive")
		@Test
		void normals() {
			assertThrows(IllegalArgumentException.class, () -> model.layout(Model.NORMALS));
		}
	}

	@DisplayName("An unindexed model...")
	@Nested
	class Unindexed {
		@BeforeEach
		void before() {
			model.layout(Point.LAYOUT);
			model.add(vertex);
			model.add(vertex);
			model.add(vertex);
			assertEquals(3, model.vertices().count());
		}

		@DisplayName("has a draw count depending on the number of vertices")
		@Test
		void add() {
			assertEquals(3, model.count());
		}

		@DisplayName("can become indexed")
		@Test
		void index() {
			model.add(0);
			assertEquals(true, model.isIndexed());
		}

		@DisplayName("cannot restart the index")
		@Test
		void restart() {
			assertThrows(IllegalStateException.class, () -> model.restart());
		}
	}

	@DisplayName("An indexed model...")
	@Nested
	class Indexed {
		@BeforeEach
		void before() {
			model.add(vertex);
			model.add(0);
			model.add(0);
			model.add(0);
		}

		@DisplayName("has a draw count depending on the size of the index")
		@Test
		void count() {
			assertEquals(3, model.count());
		}

		@DisplayName("has an index")
		@Test
		void indexed() {
			assertEquals(true, model.isIndexed());
			assertArrayEquals(new int[]{0, 0, 0}, model.index().toArray());
		}

		@DisplayName("cannot add an invalid vertex index")
		@Test
		void invalid() {
			assertThrows(IndexOutOfBoundsException.class, () -> model.add(-1));
			assertThrows(IndexOutOfBoundsException.class, () -> model.add(1));
		}

		@DisplayName("can restart the index")
		@Test
		void restart() {
			model.restart();
			assertEquals(3, model.count());
		}
	}

	@DisplayName("A model converted to a mesh...")
	@Nested
	class MeshTests {
		private Mesh mesh;

		@BeforeEach
		void before() {
			mesh = model.mesh();
		}

		@Test
		void constructor() {
			assertEquals(model, mesh.header());
		}

		@DisplayName("has a vertex buffer")
		@Test
		void vertices() {
			// Build model
			model.layout(Point.LAYOUT);
			model.add(vertex);
			model.add(vertex);
			model.add(vertex);

			// Check vertices
			final int len = 3 * 3 * 4;
			final ByteBuffer bb = ByteBuffer.allocate(len);
			final Mesh mesh = model.mesh();
			assertEquals(len, mesh.vertices().length());
			mesh.vertices().buffer(bb);
			assertEquals(0, bb.remaining());
		}

		@DisplayName("is invalid if the model layout is undefined")
		@Test
		void invalid() {
			assertThrows(IllegalStateException.class, () -> mesh.vertices());
			assertThrows(IllegalStateException.class, () -> mesh.index());
		}

		@DisplayName("is invalid if the vertex count does not match the drawing primitive")
		@Test
		void count() {
			model = new Model(Primitive.LINES);
			model.layout(Point.LAYOUT);
			model.add(vertex);
			assertThrows(IllegalStateException.class, () -> mesh.vertices());
			assertThrows(IllegalStateException.class, () -> mesh.index());
		}

		@DisplayName("does not have an index buffer if the model is unindexed")
		@Test
		void unindexed() {
			model.layout(Point.LAYOUT);
			assertEquals(Optional.empty(), mesh.index());
		}

		@DisplayName("has a short index buffer is the model is indexed and the index is small")
		@Test
		void shorts() {
			// Create index
			model.layout(Point.LAYOUT);
			model.add(vertex);
			model.add(0);
			model.add(0);
			model.add(0);

			// Check index buffer
			final int len = 3 * Short.BYTES;
			final Bufferable index = mesh.index().orElseThrow();
			final ByteBuffer bb = ByteBuffer.allocate(len);
			assertEquals(len, index.length());
			index.buffer(bb);
			assertEquals(0, bb.remaining());

			model.compact(false);
			assertEquals(3 * Integer.BYTES, index.length());
		}

		@DisplayName("has an integer index buffer is the model is indexed and the index is large")
		@Test
		void integers() {
			// Create index
			model.layout(Point.LAYOUT);
			model.add(vertex);

			// Create an index larger than the size of a short value
			final int size = 65535;
			for(int n = 0; n < size; ++n) {
				model.add(0);
			}

			// Check index is integral
			final int len = size * Integer.BYTES;
			final Bufferable index = mesh.index().orElseThrow();
			assertEquals(len, index.length());

			// Check index buffer
			final ByteBuffer bb = ByteBuffer.allocate(len);
			index.buffer(bb);
			// TODO - non-direct buffer does not get updated!!!
			// assertEquals(0, bb.remaining());
		}
	}

	@DisplayName("The bounds of a model...")
	@Nested
	class BoundsTests {
		@DisplayName("can be derived from the model vertices")
		@Test
		void bounds() {
			model.layout(Point.LAYOUT);
			model.add(vertex);
			model.add(vertex);
			assertEquals(Bounds.EMPTY, model.bounds());
		}

		@DisplayName("cannot be generated if the model layout does not contain a vertex position")
		@Test
		void layout() {
			model.add(vertex);
			assertThrows(IllegalStateException.class, () -> model.bounds());
		}

		@DisplayName("cannot be generated if any vertex does not contain a position component")
		@Test
		void vertex() {
			model.layout(Point.LAYOUT);
			model.add(Vertex.of());
			assertThrows(ArrayIndexOutOfBoundsException.class, () -> model.bounds());
		}
	}
}
