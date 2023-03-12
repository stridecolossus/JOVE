package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.volume.Bounds;

public class DefaultMeshTest {
	private DefaultMesh mesh;
	private Vertex vertex;

	@BeforeEach
	void before() {
		mesh = new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT, Normal.LAYOUT));
		vertex = new Vertex(Point.ORIGIN);
	}

	@DisplayName("A new mesh...")
	@Nested
	class New {
    	@Test
    	void constructor() {
    		assertEquals(Primitive.TRIANGLE, mesh.primitive());
    		assertEquals(new CompoundLayout(Point.LAYOUT, Normal.LAYOUT), mesh.layout());
    	}

    	@DisplayName("is not indexed")
    	@Test
    	void unindexed() {
    		assertEquals(false, mesh.isIndexed());
    	}

    	@DisplayName("initially has no vertex data")
    	@Test
    	void empty() {
    		assertEquals(0, mesh.count());
    	}

    	@DisplayName("can have vertex data added")
    	@Test
    	void add() {
    		mesh.add(vertex);
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
		}

		@DisplayName("can construct the vertex buffer")
		@Test
		void vertices() {
			final ByteSizedBufferable vertices = mesh.vertices();
			final int len = 3 * (3 + 3) * Float.BYTES;
			assertEquals(len, vertices.length());
		}

		@DisplayName("does not have an index buffer")
		@Test
		void index() {
			assertEquals(false, mesh.isIndexed());
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
			mesh.add(vertex);
			mesh.add(vertex);
			mesh.add(new Vertex(point));
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
			mesh = new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout(Normal.LAYOUT));
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
    		final MutableNormalVertex[] vertices = {
    				new MutableNormalVertex(Point.ORIGIN),
    				new MutableNormalVertex(new Point(3, 0, 0)),
    				new MutableNormalVertex(new Point(3, 3, 0))
    		};

    		// Populate model
    		for(Vertex v : vertices) {
    			mesh.add(v);
    		}

    		// Compute normals
    		mesh.compute();

    		// Check vertex normals
    		for(var v : vertices) {
    			assertEquals(Axis.Z, v.normal());
    		}
    	}

    	@DisplayName("cannot be computed if the mesh does not contain vertex data")
    	@Test
    	void vertices() {
    		mesh = new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout(Normal.LAYOUT));
    		assertThrows(IllegalStateException.class, () -> mesh.compute());
    	}

    	@DisplayName("cannot be computed if the mesh does not contain vertex normals")
    	@Test
    	void normals() {
    		mesh = new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout());
    		assertThrows(IllegalStateException.class, () -> mesh.compute());
    	}
    }
}
