package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.ByteBuffer;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;

public class IndexedModelTest {
	private IndexedModel model;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new SimpleVertex(Point.ORIGIN);
		model = new IndexedModel(Primitive.TRIANGLES, new Layout(Point.LAYOUT));
		model.add(vertex);
	}

	@Test
	void constructor() {
		assertEquals(true, model.isIndexed());
		assertEquals(0, model.count());
	}

	@DisplayName("Vertex indices can be added to an indexed model")
	@Test
	void add() {
		model.add(0);
		assertEquals(1, model.count());
		assertArrayEquals(new int[]{0}, model.index().toArray());
	}

	@DisplayName("Vertex indices must refer to valid vertices")
	@Test
	void invalid() {
		assertThrows(IndexOutOfBoundsException.class, () -> model.add(-1));
		assertThrows(IndexOutOfBoundsException.class, () -> model.add(1));
	}

	@DisplayName("The model index can be restarted")
	@Test
	void restart() {
		model.add(0);
		model.restart();
		assertEquals(1, model.count());
	}

//	@DisplayName("The faces of an indexed model...")
//	@Nested
//	class FaceIterator {
//		@BeforeEach
//		void before() {
//			model.add(0);
//			model.add(0);
//			model.add(0);
//		}
//
////		@DisplayName("can be iterated")
////		@Test
////		void faces() {
////			final Iterator<Polygon> faces = model.faces();
////			assertEquals(new Polygon(List.of(Point.ORIGIN, Point.ORIGIN, Point.ORIGIN)), faces.next());
////			assertEquals(false, faces.hasNext());
////		}
////
////		@DisplayName("cannot be iterated if the index is incomplete")
////		@Test
////		void incomplete() {
////			model.add(0);
////			assertThrows(IllegalStateException.class, () -> model.faces());
////		}
////
////		@DisplayName("skips restart indices")
////		@Test
////		void restart() {
////			model.restart();
////			final Iterator<Polygon> faces = model.faces();
////			faces.next();
////			assertEquals(false, faces.hasNext());
////		}
//	}

	@DisplayName("A indexed model that has been converted to a buffered model...")
	@Nested
	class BufferedModelTests {
		private BufferedModel buffer;

		@BeforeEach
		void before() {
			buffer = model.buffer();
		}

		@Test
		void constructor() {
			assertEquals(model, buffer);
			assertEquals(true, buffer.isIndexed());
		}

		@DisplayName("has a short index buffer if the index is small enough")
		@Test
		void shorts() {
			// Create index
			model.add(0);
			model.add(0);
			model.add(0);

			// Check index buffer
			final int len = 3 * Short.BYTES;
			final ByteSizedBufferable index = buffer.index().orElseThrow();
			final ByteBuffer bb = ByteBuffer.allocate(len);
			assertEquals(len, index.length());
			index.buffer(bb);
			assertEquals(0, bb.remaining());

			model.compact(false);
			assertEquals(3 * Integer.BYTES, index.length());
		}

		@DisplayName("has an integer index buffer if the index is large")
		@Test
		void integers() {
			// Create an index larger than the size of a short value
			final int size = 65535;
			for(int n = 0; n < size; ++n) {
				model.add(0);
			}

			// Check index is integral
			final int len = size * Integer.BYTES;
			final ByteSizedBufferable index = buffer.index().orElseThrow();
			assertEquals(len, index.length());

			// Check index buffer
			final ByteBuffer bb = ByteBuffer.allocate(len);
			index.buffer(bb);
			// TODO - non-direct buffer does not get updated!!!
			// assertEquals(0, bb.remaining());
		}
	}
}
