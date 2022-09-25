package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Volume;
import org.sarge.jove.model.Model;
import org.sarge.jove.scene.AbstractNode.Visitor;

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
		assertEquals(null, node.material());
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

//	@Test
	void inherit() {
		final GroupNode parent = new GroupNode();
		parent.add(node);
		// parent.material(mat);
		// TODO ???
	}

	@Test
	void nodes() {
		assertArrayEquals(new Node[]{node}, node.nodes().toArray());
	}

	@Test
	void visitor() {
		final Visitor visitor = mock(Visitor.class);
		node.accept(visitor);
		verify(visitor).visit(node);
	}

	@Test
	void equals() {
		assertEquals(node, node);
		assertNotEquals(node, null);
		assertNotEquals(node, new ModelNode(model));
	}
}
