package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.*;

class ComputeNormalsTest {
	private ComputeNormals compute;
	private DefaultMesh mesh;

	@BeforeEach
	void before() {
		mesh = new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT));
		compute = new ComputeNormals(mesh);
	}

	@DisplayName("The normals for a mesh can be computed from the vertex data")
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
		compute.compute();

		// Check vertex normals
		for(var v : vertices) {
			assertEquals(Axis.Z, v.normal());
		}
	}

	@DisplayName("Vertex normals cannot be computed if the primitive does not support normals")
	@Test
	void triangles() {
		mesh = new DefaultMesh(Primitive.LINE, new CompoundLayout(Point.LAYOUT));
		assertThrows(IllegalStateException.class, () -> new ComputeNormals(mesh));
	}
}
