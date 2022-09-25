package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;

/**
 * A <i>local transform</i> composes the world matrix of a node with its ancestors.
 * @author Sarge
 */
class LocalTransform implements Transform {
	private final Transform transform;
	private transient Matrix matrix;

	/**
	 * Constructor.
	 * @param transform Local transform
	 */
	LocalTransform(Transform transform) {
		this.transform = notNull(transform);
	}

	/**
	 * @return Local transform
	 */
	public Transform transform() {
		return transform;
	}

	@Override
	public boolean isMutable() {
		return transform.isMutable();
	}

	/**
	 * @return Whether this transform has been modified
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
}
