package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.volume.Bounds;

class MeshBuilderTest {
	private MeshBuilder builder;
	private Mesh mesh;
	private Vertex vertex;

	@BeforeEach
	void before() {
		vertex = new Vertex(Point.ORIGIN);
		builder = new MeshBuilder(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT));
		mesh = builder.mesh();
	}

	@DisplayName("A new mesh builder...")
	@Nested
	class New {
    	@DisplayName("can construct a mesh")
    	@Test
    	void mesh() {
    		assertNotNull(mesh);
    		assertNotNull(mesh.vertices());
    		assertEquals(Primitive.TRIANGLE, mesh.primitive());
    		assertEquals(new CompoundLayout(Point.LAYOUT), mesh.layout());
    	}

    	@DisplayName("has no vertex data")
    	@Test
    	void empty() {
    		assertEquals(0, mesh.count());
    	}

    	@DisplayName("does not have an index")
    	@Test
    	void index() {
    		assertEquals(Optional.empty(), mesh.index());
    	}

    	@DisplayName("can add vertices")
    	@Test
    	void add() {
    		builder.add(vertex);
    	}
	}

	@DisplayName("A mesh containing vertex data...")
	@Nested
	class VertexData {
		@BeforeEach
		void before() {
			builder.add(vertex);
			builder.add(vertex);
			builder.add(vertex);
		}

		@DisplayName("has a draw count depending on the number of vertices")
		@Test
		void add() {
			assertEquals(3, builder.count());
		}

		@DisplayName("generates a vertex buffer")
		@Test
		void vertices() {
			final ByteSizedBufferable vertices = mesh.vertices();
			assertEquals(3 * 3 * Float.BYTES, vertices.length());
		}

		@DisplayName("does not have an index buffer")
		@Test
		void index() {
			assertEquals(Optional.empty(), mesh.index());
		}
	}

	@DisplayName("The bounds of a mesh...")
	@Nested
	class BoundsTests {
		@DisplayName("can be derived from the mesh vertices")
		@Test
		void bounds() {
			final Point point = new Point(1, 2, 3);
			builder.add(vertex);
			builder.add(vertex);
			builder.add(new Vertex(point));
			assertEquals(new Bounds(Point.ORIGIN, point), builder.bounds());
		}

		@DisplayName("is empty if the mesh contains the same vertex")
		@Test
		void empty() {
			builder.add(vertex);
			builder.add(vertex);
			builder.add(vertex);
			assertEquals(Bounds.EMPTY, builder.bounds());
		}
	}

	@DisplayName("Vertex normals for a mesh...")
	@Nested
	class ComputeNormals {
    	@DisplayName("can be computed if the mesh primitive is a triangle")
    	@Test
    	void compute() {
    		// Create triangle
    		final MutableNormalVertex[] vertices = {
    				new MutableNormalVertex(Point.ORIGIN),
    				new MutableNormalVertex(new Point(3, 0, 0)),
    				new MutableNormalVertex(new Point(3, 3, 0))
    		};

    		// Populate model
    		for(Vertex v : vertices) {
    			builder.add(v);
    		}

    		// Compute normals
    		builder.compute();

    		// Check vertex normals
    		for(var v : vertices) {
    			assertEquals(Axis.Z, v.normal());
    		}
    	}
    }
}
