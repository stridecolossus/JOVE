package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.*;

public class DefaultVisitorTest {
	private DefaultVisitor visitor;
	private Node node;

	@BeforeEach
	void before() {
		visitor = new DefaultVisitor();
		node = new Node();
	}

	@Test
	void visit() {
		node.material().set(mock(Material.class)); // TODO
		visitor.visit(node);
		assertEquals(false, node.transform().isDirty());
		assertEquals(false, node.material().isDirty());
	}
}
