package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;
import org.sarge.jove.scene.AbstractNode.Visitor;

class WorldMatrixVisitorTest {
	private Visitor visitor;
	private Matrix x;

	@BeforeEach
	void before() {
		visitor = new WorldMatrixVisitor();
		x = Matrix4.translation(Axis.X);
	}

	@DisplayName("The world matrix of a node is evaluated if it has not been initialised")
	@Test
	void update() {
		final AbstractNode node = new GroupNode();
		node.transform(x);
		visitor.visit(node);
		assertEquals(false, node.transform().isDirty());
		assertEquals(x, node.transform().matrix());
	}

	@DisplayName("The world matrix of a node is updated if its transform has been modified")
	@Test
	void dirty() {
		final AbstractNode node = new GroupNode();
		node.transform().update(null);
		node.transform(x);
		visitor.visit(node);
		assertEquals(false, node.transform().isDirty());
		assertEquals(x, node.transform().matrix());
	}

	@DisplayName("The world matrix of a node is composed with its ancestors")
	@Test
	void compose() {
		// Create a scene graph
		final var a = new GroupNode();
		final var b = new GroupNode();
		final var c = new GroupNode();
		a.add(b);
		a.add(c);

		// Add a transform to the root
		a.transform(x);

		// Add a local transform to a leaf node
		final Matrix y = Matrix4.translation(Axis.Y);
		c.transform(y);
		c.transform().update(null);

		// Apply update
		a.accept(visitor);

		// Check scene graph is updated
		assertEquals(false, a.transform().isDirty());
		assertEquals(false, b.transform().isDirty());
		assertEquals(false, c.transform().isDirty());

		// Check world matrix
		assertEquals(x, a.transform().matrix());
		assertEquals(x, b.transform().matrix());
		assertEquals(y.multiply(x), c.transform().matrix());
	}
}
