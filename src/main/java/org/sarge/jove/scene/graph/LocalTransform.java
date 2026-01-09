package org.sarge.jove.scene.graph;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.geometry.*;

/**
 * A <i>local transform</i> is the model transformation applied to a given node and its children.
 * @author Sarge
 */
public class LocalTransform {
	/**
	 * Empty transform, i.e. the {@link Matrix#IDENTITY} matrix.
	 */
	public static final LocalTransform NONE = new LocalTransform(Matrix.IDENTITY) {
		@Override
		protected boolean isDirty() {
			return false;
		}

		@Override
		Matrix world() {
			return Matrix.IDENTITY;
		}

		@Override
		protected Matrix multiply(Matrix parent) {
			return parent;
		}
	};

	/**
	 * Creates a local transform that <b>always</b> recalculates its world matrix, e.g. for a mutable rotation.
	 * @param transform Transform
	 * @return Mutable local transform
	 */
	public static LocalTransform mutable(Transform transform) {
		return new LocalTransform(transform) {
			@Override
			protected boolean isDirty() {
				return true;
			}
		};
	}

	private final Transform transform;
	private Matrix world;

	/**
	 * Constructor.
	 * @param transform Local transform
	 */
	public LocalTransform(Transform transform) {
		this.transform = requireNonNull(transform);
	}

	/**
	 * @return Local transform
	 */
	public Transform transform() {
		return transform;
	}

	/**
	 * @return World matrix
	 */
	Matrix world() {
		return world;
	}

	/**
	 * @return Whether the world matrix needs to be updated
	 */
	protected boolean isDirty() {
		return world == null;
	}

	/**
	 *
	 */
	public static class UpdateVisitor {
		/**
		 * Updates the world matrix of the given scene graph node.
		 * @param node Node to update
		 */
		public void update(Node node) {
			final LocalTransform local = node.transform();
			if(!local.isDirty()) {
				return;
			}

			final Matrix world = world(node.parent());
			local.world = local.multiply(world);
		}

		/**
		 * Retrieves or recursively updates the world matrix of the given node.
		 * @param node Scene node
		 * @return World matrix
		 */
		private Matrix world(Node node) {
			// Stop at root node
			if(node == null) {
				return Matrix.IDENTITY;
			}

			// Otherwise update as required
			final LocalTransform transform = node.transform();
			update(node);

			return transform.world();
		}
	}

	/**
	 * Combines this transform with the given parent matrix.
	 * @param world Parent world matrix
	 * @return Local world matrix
	 */
	protected Matrix multiply(Matrix world) {
		final Matrix local = transform.matrix();
		if(world == Matrix.IDENTITY) {
			return local;
		}
		else {
			return world.multiply(local);
		}
	}

	@Override
	public String toString() {
		return String.format("Transform[transform=%s world=%s]", transform, world);
	}
}
