package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Volume;
import org.sarge.jove.model.Model;

public class ModelNodeTest {
	private ModelNode node;
	private RenderQueue queue;
	private Model model;

	@BeforeEach
	void before() {
		queue = new RenderQueue();
		model = mock(Model.class);
		node = new ModelNode(queue, model);
	}

	@Test
	void constructor() {
		assertEquals(queue, node.queue());
		assertEquals(model, node.model());
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
	void equals() {
		assertEquals(node, node);
		assertNotEquals(node, null);
		assertNotEquals(node, new ModelNode(queue, model));
	}
}
