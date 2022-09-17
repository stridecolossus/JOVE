package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;

public class NodeTest {
	private Node node;
	private Transform local;

	@BeforeEach
	void before() {
		node = new Node() {
			@Override
			public Stream<Renderable> render() {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean equals(Object obj) {
				throw new UnsupportedOperationException();
			}
		};

		local = Matrix4.translation(Axis.X);
	}

	@DisplayName("A new node...")
	@Nested
	class New {
		@DisplayName("does not have a parent")
		@Test
		void parent() {
			assertEquals(null, node.parent());
		}

		@DisplayName("has a default local transform")
		@Test
		void transform() {
			assertEquals(Matrix4.IDENTITY, node.transform());
		}

		@DisplayName("has a default world transform")
		@Test
		void matrix() {
			assertEquals(Matrix4.IDENTITY, node.matrix());
		}

		@DisplayName("does not have a bounding volume")
		@Test
		void volume() {
			assertEquals(Volume.EMPTY, node.volume());
		}
	}

	@DisplayName("A child node...")
	//@Nested
	class Child {
		private GroupNode parent;

		@BeforeEach
		void before() {
			parent = new GroupNode();
			parent.transform(Matrix4.translation(Axis.Y));
			parent.add(node);
		}

		@DisplayName("has a parent")
		@Test
		void parent() {
			assertEquals(parent, node.parent());
		}

		@DisplayName("inherits the transform of its parent")
		@Test
		void matrix() {
			assertEquals(Matrix4.translation(Axis.X.add(Axis.Y)), node.matrix());
		}

		// updated if parent changed
		// updated if local changed
	}

	@Test
	void isEqual() {
		assertEquals(true, node.isEqual(node));
	}
}
