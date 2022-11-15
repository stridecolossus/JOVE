package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.nio.ByteBuffer;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;

public class DefaultMeshTest {
	private DefaultMesh mesh;
	private Vertex vertex;

	@BeforeEach
	void before() {
		mesh = new DefaultMesh(Primitive.TRIANGLES, Point.LAYOUT);
		vertex = new SimpleVertex(Point.ORIGIN);
	}

	@DisplayName("A new default mesh...")
	@Nested
	class New {
    	@Test
    	void constructor() {
    		assertEquals(Primitive.TRIANGLES, mesh.primitive());
    		assertEquals(new Layout(Point.LAYOUT), mesh.layout());
    	}

    	@DisplayName("is not indexed")
    	@Test
    	void unindexed() {
    		assertEquals(false, mesh.isIndexed());
    	}

    	@DisplayName("is initially empty")
    	@Test
    	void empty() {
    		assertEquals(0, mesh.count());
    		assertEquals(0, mesh.vertices().count());
    	}

    	@DisplayName("can have vertex data added")
    	@Test
    	void add() {
    		mesh.add(vertex);
    	}

    	@DisplayName("cannot add vertices that do not match the layout of the mesh")
    	@Test
    	void layout() {
    		assertThrows(IllegalArgumentException.class, () -> mesh.add(mock(Vertex.class)));
    	}

    	@DisplayName("can be configured to allow vertices that do not match the layout")
    	@Test
    	void validate() {
    		mesh.validate(false);
    		mesh.add(mock(Vertex.class));
    	}
	}

	@DisplayName("A mesh containing vertex data...")
	@Nested
	class VertexData {
		@BeforeEach
		void before() {
			mesh.add(vertex);
			mesh.add(vertex);
			mesh.add(vertex);
		}

		@DisplayName("has a draw count depending on the number of vertices")
		@Test
		void add() {
			assertEquals(3, mesh.count());
			assertEquals(3, mesh.vertices().count());
		}

		@DisplayName("can enumerate the triangles comprising the mesh")
		@Test
		void triangles() {
			final Triangle triangle = new Triangle(Collections.nCopies(3, Point.ORIGIN));
			assertEquals(List.of(triangle), mesh.triangles().toList());
		}

		@DisplayName("cannot enumerate triangles if the primitive is not triangular")
		@Test
		void invalid() {
			mesh = new DefaultMesh(Primitive.LINES, new Layout());
			assertThrows(IllegalStateException.class, () -> mesh.triangles());
		}
	}

	@DisplayName("A buffered mesh...")
	@Nested
	class BufferedModelTests {
		private BufferedMesh buffer;

		@BeforeEach
		void before() {
			buffer = mesh.buffer();
		}

		@Test
		void constructor() {
			assertEquals(mesh, buffer);
			assertEquals(false, buffer.isIndexed());
		}

		@DisplayName("has a vertex buffer")
		@Test
		void vertices() {
			// Build model
			mesh.add(vertex);
			mesh.add(vertex);
			mesh.add(vertex);

			// Check vertices
			final int len = 3 * 3 * 4;
			final ByteBuffer bb = ByteBuffer.allocate(len);
			assertEquals(len, buffer.vertices().length());
			buffer.vertices().buffer(bb);
			assertEquals(0, bb.remaining());
		}
	}

	@DisplayName("The bounds of a mesh...")
	@Nested
	class BoundsTests {
		@DisplayName("can be derived from the mesh vertices")
		@Test
		void bounds() {
			final Point point = new Point(1, 2, 3);
			mesh.add(vertex);
			mesh.add(vertex);
			mesh.add(new SimpleVertex(point));
			assertEquals(new Bounds(Point.ORIGIN, point), mesh.bounds());
		}

		@DisplayName("is empty of the mesh contains the same vertex")
		@Test
		void empty() {
			mesh.add(vertex);
			mesh.add(vertex);
			mesh.add(vertex);
			assertEquals(Bounds.EMPTY, mesh.bounds());
		}

		@DisplayName("cannot be generated if the mesh layout does not contain a vertex position")
		@Test
		void layout() {
			mesh = new DefaultMesh(Primitive.TRIANGLES, Normal.LAYOUT);
			assertThrows(IllegalStateException.class, () -> mesh.bounds());
		}
	}

	@DisplayName("Vertex normals for a mesh...")
	@Nested
	class ComputeNormals {
    	@DisplayName("can be computed if the mesh primitive is a triangle")
    	@Test
    	void compute() {
    		// Create triangle
    		final var vertices = new MutableVertex[3];
    		Arrays.setAll(vertices, __ -> new MutableVertex());
    		vertices[0].position(Point.ORIGIN);
    		vertices[1].position(new Point(3, 0, 0));
    		vertices[2].position(new Point(3, 3, 0));

    		// Populate model
    		mesh = new DefaultMesh(Primitive.TRIANGLES, new Layout(Point.LAYOUT, Normal.LAYOUT));
    		mesh.validate(false);
    		for(Vertex v : vertices) {
    			mesh.add(v);
    		}

    		// Compute normals
    		mesh.compute();

    		// Check vertex normals
    		for(MutableVertex v : vertices) {
    			assertEquals(Axis.Z, v.normal());
    		}
    	}

    	@DisplayName("cannot be computed if the primitive does not support normals")
    	@Test
    	void triangles() {
    		mesh = new DefaultMesh(Primitive.LINES, new Layout(Point.LAYOUT));
    		assertThrows(IllegalStateException.class, () -> mesh.compute());
    	}

    	@DisplayName("cannot be computed if the mesh does not contain vertex data")
    	@Test
    	void vertices() {
    		mesh = new DefaultMesh(Primitive.TRIANGLES, new Layout());
    		assertThrows(IllegalStateException.class, () -> mesh.compute());
    	}

    	@DisplayName("cannot be computed if the mesh does not contains normals")
    	@Test
    	void invalid() {
    		assertThrows(IllegalStateException.class, () -> mesh.compute());
    	}
    }
}
