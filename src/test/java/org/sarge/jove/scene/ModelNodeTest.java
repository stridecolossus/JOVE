package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.EmptyVolume;
import org.sarge.jove.model.Model;

public class ModelNodeTest {
	private ModelNode node;
	private Model model;

	@BeforeEach
	void before() {
		model = mock(Model.class);
		node = new ModelNode(model);
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
		final ModelNode copy = node.copy();
		assertEquals(model, copy.model());
	}

	@Test
	void equals() {
		assertEquals(node, node);
		assertNotEquals(node, null);
		assertNotEquals(node, new ModelNode(model));
	}
}
