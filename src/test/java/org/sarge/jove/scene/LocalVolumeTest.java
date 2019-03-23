package org.sarge.jove.scene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.sarge.jove.geometry.BoundingVolume;
import org.sarge.jove.geometry.Extents;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.SphereVolume;
import org.sarge.jove.scene.LocalVolume.AggregateVolume;
import org.sarge.jove.scene.LocalVolume.CullVisitor;

public class LocalVolumeTest {
	@Nested
	class EmptyVolumeTests {
		@Test
		public void constructor() {
			assertEquals(BoundingVolume.EMPTY, LocalVolume.NONE.volume());
			assertEquals(true, LocalVolume.NONE.isVisible());
			assertEquals(false, LocalVolume.NONE.isPickable());
		}
	}

	@Nested
	class FixedVolumeTests {
		private LocalVolume vol;
		private BoundingVolume sphere;

		@BeforeEach
		public void before() {
			sphere = new SphereVolume(Point.ORIGIN, 3);
			vol = LocalVolume.of(sphere, true);
		}

		@Test
		public void constructor() {
			assertEquals(sphere, vol.volume());
			assertEquals(false, vol.isVisible());
			assertEquals(true, vol.isPickable());
		}
	}

	@Nested
	class AggregateVolumeTests {
		private AggregateVolume vol;

		@BeforeEach
		public void before() {
			vol = new AggregateVolume(SphereVolume::of, true);
		}

		@Test
		public void constructor() {
			assertEquals(BoundingVolume.EMPTY, vol.volume());
			assertEquals(false, vol.isVisible());
			assertEquals(true, vol.isPickable());
		}

		@Test
		public void update() {
			vol.update(new Extents(new Point(1, 2, 3), new Point(5, 6, 7)));
			assertEquals(new SphereVolume(new Point(3, 4, 5), 2), vol.volume());
		}

		@Test
		public void propagate() {
			// Create a parent node with an aggregate volume
			final Node parent = new Node("parent");
			parent.volume(vol);

			// Add a child with a volume
			final BoundingVolume sphere = new SphereVolume(Point.ORIGIN, 3);
			final Node child = new Node("child");
			child.volume(LocalVolume.of(sphere, true));
			parent.add(child);

			// Propagate modified volume
			LocalVolume.propagate(child);
			assertEquals(sphere, vol.volume());
		}
	}

	@Nested
	class CullVisitorTests {
		private CullVisitor visitor;
		private Frustum frustum;
		private Node node;

		@BeforeEach
		public void before() {
			frustum = mock(Frustum.class);
			visitor = new CullVisitor(frustum);
			node = new Node("node");
		}

		@Test
		public void visitEmptyVolume() {
			node.volume(LocalVolume.NONE);
			visitor.visit(node);
			assertEquals(true, node.volume().isVisible());
		}

		@Test
		public void visit() {
			node.volume(LocalVolume.NONE);
			visitor.visit(node);
			assertEquals(true, node.volume().isVisible());
		}

		@Test
		public void visitNotVisible() {
			final BoundingVolume vol = mock(BoundingVolume.class);
			when(vol.intersects(frustum)).thenReturn(true);
			node.volume(LocalVolume.of(vol, true));
			visitor.visit(node);
			assertEquals(true, node.volume().isVisible());
		}
	}
}
