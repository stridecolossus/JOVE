package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;

class MeshLoaderTest {
	private MeshLoader loader;

	@BeforeEach
	void before() {
		loader = new MeshLoader();
	}

	@Test
	void load() throws IOException {
		// Create an indexed mesh
		final var mesh = new IndexedMesh(Primitive.TRIANGLE, List.of(Point.LAYOUT));
		mesh.add(new Vertex(Point.ORIGIN));
		mesh.add(0);
		mesh.add(0);
		mesh.add(0);

		// Write mesh
		final var out = new ByteArrayOutputStream();
		loader.save(mesh, new DataOutputStream(out));

		// Reload and check is same
		final Mesh result = loader.load(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
		assertEquals(Primitive.TRIANGLE, result.primitive());
		assertEquals(List.of(Point.LAYOUT), result.layout());
		assertEquals(3, result.vertices().asFloatBuffer().limit());
		assertEquals(3, result.index().orElseThrow().asIntBuffer().limit());
	}

	@Test
	void version() throws IOException {
		final var out = new ByteArrayOutputStream();
		new DataOutputStream(out).writeInt(2);
		final var in = new DataInputStream(new ByteArrayInputStream(out.toByteArray()));
		assertThrows(IOException.class, () -> loader.load(in));
	}
}
