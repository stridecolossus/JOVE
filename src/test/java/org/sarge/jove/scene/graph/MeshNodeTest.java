package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.model.Mesh;

class MeshNodeTest {
	private MeshNode node;
	private RootNode root;
	private Mesh mesh;
	private Material mat;

	@BeforeEach
	void before() {
		mesh = mock(Mesh.class);
		mat = mock(Material.class);
		root = new RootNode();
		node = new MeshNode(root, mesh, mat);
	}

	@Test
	void constructor() {
		assertEquals(root, node.parent());
		assertEquals(mesh, node.mesh());
		assertEquals(mat, node.material());
	}

	@DisplayName("TODO")
	@Test
	void attach() {
		assertEquals(List.of(node), root.nodes().toList());
//		assertEquals(List.of(node), root.nodes(mat));
	}

	@DisplayName("A mesh node can be detached from the scene graph")
	@Test
	void detach() {
		node.detach();
		assertEquals(null, node.parent());
		assertEquals(List.of(), root.nodes().toList());
//		assertEquals(List.of(), root.nodes(mat));
	}
}
