package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractNode implements SceneGraph, Node {
	private AbstractNode parent;
	private Volume vol = Volume.EMPTY;
	private Transform transform = Transform.IDENTITY;
	private transient Matrix matrix;

	/**
	 * @return Parent of this node
	 */
	public Node parent() {
		return parent;
	}

	/**
	 * Attaches this node to the given parent;
	 * @param parent New parent
	 * @throws IllegalStateException if this node is already attached
	 */
	protected final void attach(AbstractNode parent) {
		assert parent != null;
		if(this.parent != null) throw new IllegalStateException("Node is already attached: " + this);
		this.parent = parent;
	}

	/**
	 * Detaches this node from its parent.
	 */
	protected final void detach() {
		assert parent != null;
		parent = null;
	}

	@Override
	public Transform transform() {
		return transform;
	}

	/**
	 * Sets the local transform of this node.
	 * @param transform Local transform or {@link Transform#IDENTITY} if none
	 */
	public void transform(Transform transform) {
		this.transform = notNull(transform);
		this.matrix = null;
	}

	/**
	 * @return Whether the transform of this node has been modified
	 */
	private boolean isDirty() {
		return (matrix == null) || transform.isDirty() || isParentDirty();
	}

	private boolean isParentDirty() {
		if(parent == null) {
			return false;
		}
		else {
			return parent.isDirty();
		}
	}

	@Override
	public Matrix matrix() {
		if(isDirty()) {
			matrix = update();
		}
		return matrix;
	}

	/**
	 * Updates the world matrix of this node.
	 */
	private Matrix update() {
		final Matrix t = transform.matrix();
		if(parent == null) {
			return t;
		}

		final Matrix p = parent.matrix();
		if(p == Transform.IDENTITY) {
			return t;
		}
		else {
			return p.multiply(t);
		}
	}

	@Override
	public Volume volume() {
		return vol;
	}

	/**
	 * Sets the bounding volume of this node.
	 * @param vol Bounding volume or {@link Volume#EMPTY} if none
	 */
	public void volume(Volume vol) {
		this.vol = notNull(vol);
	}

	@Override
	public int hashCode() {
		return Objects.hash(transform, vol);
	}

	@Override
	public boolean equals(Object obj) {
		throw new UnsupportedOperationException();
	}

	protected final boolean isEqual(AbstractNode that) {
		return this.transform.equals(that.transform) && this.vol.equals(that.vol);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(transform)
				.append(vol)
				.build();
	}
}
