package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.model.Mesh;

@Disabled("TODO")
class RootNodeTest {
	private RootNode root;
	private Mesh mesh;
	private Material mat;

	@BeforeEach
	void before() {
		root = new RootNode();
		// mesh = new Mesh(null, null, null, null, null); //mock(Mesh.class);
		mat = mock(Material.class);
	}

	@Test
	void constructor() {
		assertEquals(null, root.parent());
		assertEquals(root, root.root());
	}

	@Test
	void attach() {
		final MeshNode node = new MeshNode(root, mesh, mat);
		assertEquals(List.of(node), root.nodes().toList());
		// TODO
	}

	@Test
	void detach() {
		final MeshNode node = new MeshNode(root, mesh, mat);
		node.detach();
		assertEquals(List.of(), root.nodes().toList());
	}
}
