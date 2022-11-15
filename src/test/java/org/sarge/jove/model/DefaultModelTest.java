package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;

public class DefaultModelTest {
	private DefaultModel model;
	private Vertex vertex;

	@BeforeEach
	void before() {
		model = new DefaultModel(Primitive.TRIANGLES, Point.LAYOUT);
		vertex = new SimpleVertex(Point.ORIGIN);
	}

	@DisplayName("A new default model...")
	@Nested
	class New {
    	@Test
    	void constructor() {
    		assertEquals(Primitive.TRIANGLES, model.primitive());
    		assertEquals(new Layout(Point.LAYOUT), model.layout());
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
    	}

    	@DisplayName("can have vertex data added")
    	@Test
    	void add() {
    		model.add(vertex);
    	}

    	@DisplayName("cannot add vertices that do not match the layout of the model")
    	@Test
    	void layout() {
    		assertThrows(IllegalArgumentException.class, () -> model.add(mock(Vertex.class)));
    	}

    	@DisplayName("TODO")
    	@Test
    	void validate() {
    		model.validate(false);
    		model.add(mock(Vertex.class));
    	}
	}

	@DisplayName("A model containing vertex data...")
	@Nested
	class Unindexed {
		@BeforeEach
		void before() {
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

//		@DisplayName("can iterate over the faces of the model")
//		@Test
//		void faces() {
//			final Iterator<Polygon> faces = model.faces();
//			assertEquals(new Polygon(List.of(Point.ORIGIN, Point.ORIGIN, Point.ORIGIN)), faces.next());
//			assertEquals(false, faces.hasNext());
//		}
//
//		@DisplayName("cannot iterate over the faces of the model if the vertices are incomplete")
//		@Test
//		void incomplete() {
//			model.add(vertex);
//			assertThrows(IllegalStateException.class, () -> model.faces());
//		}
	}

//	// TODO - DO WE NEED THIS TO BE SEPARATE NESTED?
//	@DisplayName("A model that does not contain vertex positions...")
//	@Nested
//	class MissingPosition {
//		@BeforeEach
//		void before() {
//			model.add(vertex);
//		}
//
//		@DisplayName("cannot iterate over the faces of the model")
//		@Test
//		void faces() {
//			assertThrows(IllegalStateException.class, () -> model.faces());
//		}
//	}


	@Test
	void triangles() {
		model.add(vertex);
		model.add(vertex);
		model.add(vertex);
		final Triangle triangle = new Triangle(Collections.nCopies(3, Point.ORIGIN));
		assertEquals(List.of(triangle), model.triangles().toList());
	}

	@DisplayName("A buffered model...")
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
			assertEquals(false, buffer.isIndexed());
		}

		@DisplayName("has a vertex buffer")
		@Test
		void vertices() {
			// Build model
			model.add(vertex);
			model.add(vertex);
			model.add(vertex);

			// Check vertices
			final int len = 3 * 3 * 4;
			final ByteBuffer bb = ByteBuffer.allocate(len);
			assertEquals(len, buffer.vertices().length());
			buffer.vertices().buffer(bb);
			assertEquals(0, bb.remaining());
		}
	}

	@DisplayName("The bounds of a model...")
	@Nested
	class BoundsTests {
		@DisplayName("can be derived from the model vertices")
		@Test
		void bounds() {
			final Point point = new Point(1, 2, 3);
			model.add(vertex);
			model.add(vertex);
			model.add(new SimpleVertex(point));
			assertEquals(new Bounds(Point.ORIGIN, point), model.bounds());
		}

		@DisplayName("is empty of the model contains the same vertex")
		@Test
		void empty() {
			model.add(vertex);
			model.add(vertex);
			model.add(vertex);
			assertEquals(Bounds.EMPTY, model.bounds());
		}

		@DisplayName("cannot be generated if the model layout does not contain a vertex position")
		@Test
		void layout() {
			model = new DefaultModel(Primitive.TRIANGLES, Normal.LAYOUT);
			assertThrows(IllegalStateException.class, () -> model.bounds());
		}
	}

	@DisplayName("Vertex normals for a model...")
	@Nested
	class ComputeNormals {
    	@DisplayName("can be computed if the model primitive is a triangle")
    	@Test
    	void compute() {
    		// Create triangle
    		final var vertices = new MutableVertex[3];
    		Arrays.setAll(vertices, __ -> new MutableVertex());
    		vertices[0].position(Point.ORIGIN);
    		vertices[1].position(new Point(3, 0, 0));
    		vertices[2].position(new Point(3, 3, 0));

    		// Populate model
    		model = new DefaultModel(Primitive.TRIANGLES, new Layout(Point.LAYOUT, Normal.LAYOUT));
    		model.validate(false);
    		for(Vertex v : vertices) {
    			model.add(v);
    		}

    		// Compute normals
    		model.compute();

    		// Check vertex normals
    		for(MutableVertex v : vertices) {
    			assertEquals(Axis.Z, v.normal());
    		}
    	}

    	@DisplayName("cannot be computed if the primitive does not support normals")
    	@Test
    	void triangles() {
    		model = new DefaultModel(Primitive.LINES, new Layout(Point.LAYOUT));
    		assertThrows(IllegalStateException.class, () -> model.compute());
    	}

    	@DisplayName("cannot be computed if the model that does not contain vertex data")
    	@Test
    	void vertices() {
    		model = new DefaultModel(Primitive.TRIANGLES, new Layout());
    		assertThrows(IllegalStateException.class, () -> model.compute());
    	}

    	@DisplayName("cannot be computed if the model does not contains normals")
    	@Test
    	void invalid() {
    		assertThrows(IllegalStateException.class, () -> model.compute());
    	}
    }
}
