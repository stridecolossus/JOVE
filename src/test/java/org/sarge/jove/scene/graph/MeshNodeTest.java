package org.sarge.jove.scene.graph;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.model.Mesh;
import org.sarge.jove.scene.graph.MeshNode;
import org.sarge.jove.scene.volume.EmptyVolume;

public class MeshNodeTest {
	private MeshNode node;
	private Mesh model;

	@BeforeEach
	void before() {
		model = mock(Mesh.class);
		node = new MeshNode(model);
	}

	@Test
	void constructor() {
		assertEquals(model, node.model());
		assertEquals(EmptyVolume.INSTANCE, node.volume());
	}

	@Test
	void root() {
		assertEquals(null, node.parent());
		assertEquals(true, node.isRoot());
	}

	@Test
	void attach() {
		// TODO
	}

	@Test
	void detach() {
		// TODO
	}

	@Test
	void copy() {
		final MeshNode copy = node.copy();
		assertEquals(model, copy.model());
	}

	@Test
	void equals() {
		assertEquals(node, node);
		assertNotEquals(node, null);
		assertNotEquals(node, new MeshNode(model));
	}
}
