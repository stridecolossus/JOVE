package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.*;
import org.sarge.jove.geometry.Matrix.Matrix4;

/**
 * A <i>node</i> is a element of a scene graph.
 * @author Sarge
 */
public abstract class Node {
	protected Node parent;
	protected Volume vol = Volume.EMPTY;
	protected Transform transform = Matrix4.IDENTITY;

	private transient Matrix matrix;

	/**
	 * @return Parent of this node
	 */
	public final Node parent() {
		return parent;
	}

	/**
	 * Detaches this node from its parent.
	 */
	protected final void detach() {
		assert parent != null;
		parent = null;
	}

	/**
	 * @return Local transform of this node
	 */
	public Transform transform() {
		return transform;
	}

	/**
	 * Sets the local transform of this node.
	 * @param transform Local transform or {@link Matrix4#IDENTITY} if none
	 */
	public void transform(Transform transform) {
		this.transform = notNull(transform);
		this.matrix = null;
	}

	/**
	 * @return World matrix of this node
	 */
	public final Matrix matrix() {
		return matrix;
	}

	/**
	 * @return Bounding volume of this node
	 */
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

	/**
	 * Flattens this node to the renderable objects.
	 * @return Renderable objects
	 */
	public abstract Stream<Renderable> render();

	@Override
	public int hashCode() {
		return Objects.hash(transform, vol);
	}

	@Override
	public abstract boolean equals(Object obj);

	protected final boolean isEqual(Node that) {
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
