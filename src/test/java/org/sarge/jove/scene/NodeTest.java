package org.sarge.jove.scene;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.BoundingVolume;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.SphereVolume;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.Material;
import org.sarge.jove.scene.Node.Visitor;

public class NodeTest {
	private Node node, child;

	@BeforeEach
	public void before() {
		node = new Node("parent");
		child = new Node("child");
	}

	@Test
	public void constructor() {
		assertEquals("parent", node.name());
		assertEquals(null, node.parent());
	}

	@Nested
	class PropertyTests {
		@Test
		public void transform() {
			assertNotNull(node.transform());
			assertEquals(Matrix.IDENTITY, node.transform().transform());
		}

		@Test
		public void setTransform() {
			final Matrix matrix = Matrix.translation(Vector.X_AXIS);
			final LocalTransform transform = new LocalTransform(matrix);
			transform.update(Matrix.IDENTITY);
			node.transform(transform);
			assertEquals(transform, node.transform());
			assertEquals(matrix, node.transform().transform());
		}

		@Test
		public void volume() {
			assertNotNull(node.volume());
			assertEquals(BoundingVolume.EMPTY, node.volume().volume());
		}

		@Test
		public void setVolume() {
			final BoundingVolume sphere = new SphereVolume(Point.ORIGIN, 3);
			final LocalVolume vol = LocalVolume.of(sphere, true);
			node.volume(vol);
			assertEquals(vol, node.volume());
			assertEquals(sphere, node.volume().volume());
		}
	}

	@Nested
	class ModelEntryTests {
		private RenderQueue queue;

		@BeforeEach
		public void before() {
			queue = new RenderQueue(RenderQueue.Order.NONE);
		}

		@Test
		public void model() {
			assertEquals(RenderQueue.Entry.NONE, node.model());
		}

		@Test
		public void setModel() {
			final RenderQueue.Entry model = new RenderQueue.Entry(mock(RenderQueue.Renderable.class), queue);
			node.model(model);
			assertEquals(model, node.model());
			assertArrayEquals(new Node[]{node}, queue.queue(Point.ORIGIN).toArray());
		}

		@Test
		public void setModelRemove() {
			final RenderQueue.Entry model = new RenderQueue.Entry(mock(RenderQueue.Renderable.class), queue);
			node.model(model);
			node.model(RenderQueue.Entry.NONE);
			assertEquals(0, queue.queue(Point.ORIGIN).count());
		}
	}

	@Nested
	class MaterialTests {
		private Material mat;

		@BeforeEach
		public void before() {
			mat = new Material.Builder("mat").build();
		}

		@Test
		public void material() {
			assertEquals(Material.NONE, node.material());
		}

		@Test
		public void setMaterial() {
			node.material(mat);
			assertEquals(mat, node.material());
		}

		@Test
		public void setMaterialNone() {
			node.material(mat);
			node.material(Material.NONE);
			assertEquals(Material.NONE, node.material());
		}

		@Test
		public void setMaterialAncestor() {
			node.add(child);
			node.material(mat);
			assertEquals(mat, node.material());
			assertEquals(mat, child.material());
		}

		@Test
		public void setMaterialOverride() {
			node.add(child);
			child.material(mat);
			assertEquals(Material.NONE, node.material());
			assertEquals(mat, child.material());
		}
	}

	@Nested
	class SceneGraphTests {
		@Test
		public void children() {
			assertNotNull(node.children());
			assertEquals(0, node.children().count());
		}

		@Test
		public void add() {
			node.add(child);
			assertEquals(node, child.parent());
			assertEquals(1, node.children().count());
			assertEquals(child, node.children().iterator().next());
			assertEquals(0, child.children().count());
		}

		@Test
		public void addAlreadyAdded() {
			final Node other = new Node("other");
			other.add(child);
			assertThrows(IllegalArgumentException.class, () -> node.add(child));
		}

		@Test
		public void addSelf() {
			assertThrows(IllegalArgumentException.class, () -> node.add(node));
		}

		@Test
		public void remove() {
			node.add(child);
			child.remove();
			assertEquals(null, child.parent());
			assertEquals(0, node.children().count());
		}

		@Test
		public void removeNotAdded() {
			assertThrows(IllegalStateException.class, () -> node.remove());
		}

		@Test
		public void clear() {
			node.add(child);
			node.clear();
			assertEquals(null, child.parent());
			assertEquals(0, node.children().count());
		}
	}

	@Nested
	class VisitorTests {
		private Visitor visitor;

		@BeforeEach
		public void before() {
			visitor = mock(Visitor.class);
		}

		@Test
		public void visitor() {
			when(visitor.visit(node)).thenReturn(true);
			node.add(child);
			node.accept(visitor);
			verify(visitor).visit(node);
			verify(visitor).visit(child);
		}

		@Test
		public void visitorStopRecursion() {
			node.add(child);
			node.accept(visitor);
			verify(visitor).visit(node);
			verifyNoMoreInteractions(visitor);
		}
	}
}
