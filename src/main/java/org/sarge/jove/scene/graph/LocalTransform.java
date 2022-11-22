package org.sarge.jove.scene.graph;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;

/**
 * A <i>local transform</i> composes the world matrix of a node with its ancestors.
 * @author Sarge
 */
public class LocalTransform extends InheritedProperty<LocalTransform> {
	private Transform transform = Matrix4.IDENTITY;
	private transient Matrix matrix;

	/**
	 * Constructor.
	 */
	LocalTransform() {
	}

	/**
	 * Copy constructor.
	 * @param that Local transform to copy
	 */
	LocalTransform(LocalTransform that) {
		this.transform = that.transform;
	}

	/**
	 * @return Local transform
	 */
	public Transform transform() {
		return transform;
	}

	@Override
	public boolean isDirty() {
		return matrix == null;
	}

	/**
	 * Sets the local transform at this node.
	 * @param transform Local transform
	 */
	public void set(Transform transform) {
		this.transform = notNull(transform);
		reset();
	}

	/**
	 * Resets this local transform to the <i>dirty</i> state.
	 */
	void reset() {
		matrix = null;
	}

	/**
	 * @return World matrix at this node
	 * @throws IllegalStateException if this local transform has not been updated
	 */
	Matrix matrix() {
		if(isDirty()) throw new IllegalStateException("Local transform has not been updated: " + this);
		// TODO - special case for ancestor that can become dirty
		// => ref to the most distant mutable transform
		// => needs to propagate down from it
		return matrix;
	}

	@Override
	void update(LocalTransform parent) {
		this.matrix = evaluate(parent);
	}

	/**
	 * Composes the world matrix with the given parent.
	 * @return World matrix for this transform
	 */
	private Matrix evaluate(LocalTransform parent) {
		final Matrix m = transform.matrix();
		if(parent == null) {
			return m;
		}
		else {
			final Matrix p = parent.matrix();
			if(m == Matrix4.IDENTITY) {
				return p;
			}
			else {
				return p.multiply(m);
			}
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
