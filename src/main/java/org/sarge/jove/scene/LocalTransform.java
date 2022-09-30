package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;

/**
 * A <i>local transform</i> composes the world matrix of a node with its ancestors.
 * @author Sarge
 */
public class LocalTransform implements Transform {
	private Transform transform = Matrix4.IDENTITY;
	private transient Matrix matrix;

	/**
	 * @return Local transform
	 */
	public Transform transform() {
		return transform;
	}

	/**
	 * Sets that local transform.
	 * @param transform Local transform
	 */
	public void set(Transform transform) {
		this.transform = notNull(transform);
	}

	@Override
	public boolean isMutable() {
		return transform.isMutable();
	}

	/**
	 * @return Whether this local transform has been initialised
	 */
	boolean isDirty() {
		return matrix == null;
	}

	@Override
	public Matrix matrix() {
		// TODO - special case for ancestor that can become dirty
		// => ref to the most distant mutable transform
		// => needs to propagate down from it
		return matrix;
	}

	/**
	 * Updates this transform.
	 * @param parent Parent transform
	 */
	void update(LocalTransform parent) {
		this.matrix = evaluate(parent);
	}

	/**
	 * @return World matrix for this transform
	 */
	private Matrix evaluate(LocalTransform parent) {
		final Matrix m = transform.matrix();
		if(parent == null) {
			return m;
		}
		else {
			return parent.matrix().multiply(m);
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("dirty", isDirty())
				.append(transform)
				.build();
	}

	/**
	 * The <i>world matrix visitor</i> updates the world matrix of a scene graph.
	 * @see AbstractNode#transform(Transform)
	 */
	public static class WorldMatrixVisitor {
		private LocalTransform parent;
		private boolean dirty;

		/**
		 * Updates the given local transform.
		 * @param transform Local transform to update
		 */
		public void update(LocalTransform transform) {
			// Compose transform with parent
			if(dirty || transform.isDirty()) {
				transform.update(parent);
				dirty = true;
			}

			// Record latest transform
			if(transform.transform != Matrix4.IDENTITY) {
				parent = transform;
			}
		}
	}
}
