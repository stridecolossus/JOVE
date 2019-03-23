package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.BoundingVolume;
import org.sarge.jove.geometry.Extents;

/**
 * A <i>local volume</i> defines the bounding volume strategy for a node.
 * @author Sarge
 */
public interface LocalVolume {
	/**
	 * @return Bounding volume
	 */
	BoundingVolume volume();

	/**
	 * @return Whether this volume is visible
	 */
	boolean isVisible();

	/**
	 * @return Whether this volume can be picked
	 */
	boolean isPickable();

	/**
	 * Empty bounding volume that is always visible but cannot be picked.
	 * @see BoundingVolume#EMPTY
	 */
	LocalVolume NONE = new LocalVolume() {
		@Override
		public BoundingVolume volume() {
			return BoundingVolume.EMPTY;
		}

		@Override
		public boolean isVisible() {
			return true;
		}

		@Override
		public boolean isPickable() {
			return false;
		}
	};

	// TODO - private JDK12
	/**
	 * Partial implementation.
	 */
	abstract class AbstractLocalVolume implements LocalVolume {
		private final boolean pick;

		private boolean visible;

		/**
		 * Constructor.
		 * @param vol Volume
		 */
		protected AbstractLocalVolume(boolean pick) {
			this.pick = pick;
		}

		@Override
		public final boolean isVisible() {
			return visible;
		}

		@Override
		public final boolean isPickable() {
			return pick;
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	/**
	 * Creates a fixed bounding volume.
	 * @param vol		Bounding volume
	 * @param pick		Whether this volume can be picked
	 */
	static LocalVolume of(BoundingVolume vol, boolean pick) {
		return new AbstractLocalVolume(pick) {
			@Override
			public BoundingVolume volume() {
				return vol;
			}
		};
	}

	/**
	 * Aggregates the volumes of the children of a node.
	 */
	class AggregateVolume extends AbstractLocalVolume {
		/**
		 * Factory for an aggregate volume.
		 */
		interface Factory {
			/**
			 * Creates an aggregate bounding volume containing the given extents.
			 * @param extents Extents
			 * @return Volume
			 */
			BoundingVolume create(Extents extents);
		}

		private final Factory factory;

		private BoundingVolume vol = BoundingVolume.EMPTY;

		/**
		 * Constructor.
		 * @param factory 		Volume factory
		 * @param vol			Initial bounding volume
		 */
		public AggregateVolume(Factory factory, boolean pick) {
			super(pick);
			this.factory = notNull(factory);
		}

		@Override
		public BoundingVolume volume() {
			return vol;
		}

		/**
		 * Updates the extents of this volume.
		 * @param extents Aggregated extents of the children
		 */
		void update(Extents extents) {
			vol = factory.create(extents);
		}
	}

	/**
	 * Updates the bounding volume at the given node and propagates to the root of the scene-graph.
	 * @param start starting node
	 */
	static void propagate(Node start) {
		Node node = notNull(start);
		while(true) {
			// Aggregate extents of children
			final LocalVolume local = node.volume();
			if(local instanceof AggregateVolume) {
				final AggregateVolume aggregate = (AggregateVolume) local;
				final Extents extents = aggregate(node);
				aggregate.update(extents);
			}

			// Propagate to parent
			node = node.parent();

			// Stop at root
			if(node == null) {
				break;
			}
		}
	}

	/**
	 * Calculates the aggregated extents of the children of the given node.
	 * @param node Node
	 * @return Aggregated extents
	 */
	private static Extents aggregate(Node node) {
		return node.children()
			.map(Node::volume)
			.map(LocalVolume::volume)
			.map(BoundingVolume::extents)
			.reduce(Extents::add)
			.orElse(Extents.EMPTY);
	}

	/**
	 * Visitor that updates the culled state of a scene-graph.
	 * @see BoundingVolume#intersects(Frustum)
	 */
	class CullVisitor implements Node.Visitor {
		private final Frustum frustum;

		/**
		 * Constructor.
		 * @param frustum View frustum
		 */
		public CullVisitor(Frustum frustum) {
			this.frustum = notNull(frustum);
		}

		@Override
		public boolean visit(Node node) {
			// Ignore empty volumes
			final LocalVolume local = node.volume();
			if(local == LocalVolume.NONE) {
				return true;
			}

			// Update cull state
			final AbstractLocalVolume vol = (AbstractLocalVolume) local;
			vol.visible = vol.volume().intersects(frustum);

			// Recurse is this node is visible
			return vol.visible;
		}
	}
}
