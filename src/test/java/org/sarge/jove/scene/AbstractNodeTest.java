package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;
import org.sarge.jove.scene.AbstractNode.Visitor;

public class AbstractNodeTest {
	private AbstractNode node;
	private Transform transform;

	@BeforeEach
	void before() {
		node = new AbstractNode() {
			@Override
			public Stream<AbstractNode> nodes() {
				return Stream.empty();
			}
		};
		transform = Matrix4.translation(Axis.X);
	}

	@DisplayName("A new node does not have a parent")
	@Test
	void parent() {
		assertEquals(null, node.parent());
		assertEquals(true, node.isRoot());
	}

	@DisplayName("A node has a default local transform")
	@Test
	void identity() {
		final LocalTransform transform = node.transform();
		assertNotNull(transform);
		transform.update(null);
		assertEquals(Matrix4.IDENTITY, node.transform().matrix());
	}

	@DisplayName("can have a local transform applied")
	@Test
	void transform() {
//		node.transform(transform);
		node.transform().update(null);
		assertEquals(transform, node.transform().matrix());
	}

	// TODO
	// - materials
	// - set volume

	@DisplayName("does not have a bounding volume")
	@Test
	void volume() {
		assertEquals(Volume.EMPTY, node.volume());
	}

	@DisplayName("A node can be visited")
	@Test
	void visitor() {
		final Visitor visitor = mock(Visitor.class);
		node.accept(visitor);
		verify(visitor).visit(node);
	}

	@Test
	void equals() {
		assertEquals(true, node.equals(node));
		assertEquals(false, node.equals(null));
		assertEquals(false, node.equals(mock(AbstractNode.class)));
	}
}
