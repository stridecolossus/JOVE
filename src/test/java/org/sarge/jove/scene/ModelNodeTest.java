package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import java.util.Optional;

import org.junit.jupiter.api.*;
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
		assertEquals(Optional.empty(), node.material());
	}

	@Test
	void material() {
		final Material mat = new Material();
		node.material(mat);
		assertEquals(Optional.of(mat), node.material());
	}

	@Test
	void render() {
		assertArrayEquals(new Node[]{node}, node.render().toArray());
	}

	@Test
	void equals() {
		assertEquals(node, node);
		assertEquals(node, new ModelNode(model));
		assertNotEquals(node, null);
		assertNotEquals(node, new ModelNode(mock(Model.class)));
	}
}
