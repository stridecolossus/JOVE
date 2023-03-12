package org.sarge.jove.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.io.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Point;

class MeshLoaderTest {
	private MeshLoader loader;
	private ByteArrayOutputStream out;

	@BeforeEach
	void before() {
		out = new ByteArrayOutputStream();
		loader = new MeshLoader();
	}

	@SuppressWarnings("resource")
	@Test
	void map() throws IOException {
		assertNotNull(loader.map(mock(InputStream.class)));
	}

	private Mesh read() throws IOException {
		return loader.load(new DataInputStream(new ByteArrayInputStream(out.toByteArray())));
	}

	@Test
	void load() throws IOException {
		// Create an indexed mesh
		final var mesh = new IndexedMesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT));
		mesh.add(new Vertex(Point.ORIGIN));
		mesh.add(0);
		mesh.add(0);
		mesh.add(0);

		// Write mesh
		loader.save(mesh, new DataOutputStream(out));

		// Reload and check is same
		final Mesh result = read();
		assertEquals(mesh, result);

		// Check vertices
		final ByteSizedBufferable vertices = result.vertices();
		assertEquals(3 * Float.BYTES, vertices.length());

		// Check index
		final ByteSizedBufferable index = result.index().orElseThrow();
		assertEquals(3 * Short.BYTES, index.length());
	}

	@Test
	void loadUnsupportedVersion() throws IOException {
		new DataOutputStream(out).writeInt(2);
		assertThrows(UnsupportedOperationException.class, () -> read());
	}
}
