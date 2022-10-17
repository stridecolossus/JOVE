package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Volume;
import org.sarge.jove.model.Mesh;

public class ModelNodeTest {
	private ModelNode node;
	private RenderQueue queue;
	private Mesh mesh;

	@BeforeEach
	void before() {
		queue = new RenderQueue();
		mesh = mock(Mesh.class);
		node = new ModelNode(queue, mesh);
	}

	@Test
	void constructor() {
		assertEquals(queue, node.queue());
		assertEquals(mesh, node.mesh());
		assertEquals(Volume.EMPTY, node.volume());
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
		final ModelNode copy = node.copy();
		assertEquals(queue, copy.queue());
		assertEquals(mesh, copy.mesh());
	}

	@Test
	void equals() {
		assertEquals(node, node);
		assertNotEquals(node, null);
		assertNotEquals(node, new ModelNode(queue, mesh));
	}
}
