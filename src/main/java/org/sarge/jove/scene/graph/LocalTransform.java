package org.sarge.jove.scene.graph;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.geometry.*;

/**
 * A <i>local transform</i> is the model transform applied to a given node and its children.
 * @author Sarge
 */
public class LocalTransform {
	/**
	 * Empty transform, i.e. {@link Matrix#IDENTITY}.
	 */
	public static final LocalTransform NONE = new LocalTransform(Matrix.IDENTITY) {
		@Override
		protected Matrix multiply(Matrix parent) {
			return parent;
		}
	};

	/**
	 * Creates a local transform that always recalculates its world matrix, e.g. for a mutable rotation.
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
	 * Copy constructor.
	 * @param that Local transform to copy
	 */
	protected LocalTransform(LocalTransform that) {
		this(that.transform);
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
	 * Clears the world matrix of this transform.
	 */
	void clear() {
		world = null;
	}

	/**
	 * Updates the world matrix of this transform.
	 * @param node Companion scene node
	 */
	void update(Node node) {
		// Stop if already updated
		if(!isDirty()) {
			return;
		}

		// Otherwise combine local transform with world matrix
		final Matrix parent = parent(node.parent());
		this.world = multiply(parent);
	}

	/**
	 * Combines this transform with the given parent world matrix.
	 * @param parent Parent world matrix
	 * @return World matrix
	 */
	protected Matrix multiply(Matrix parent) {
		final Matrix local = transform.matrix();
		if(parent == Matrix.IDENTITY) {
			return local;
		}
		else {
			return parent.multiply(local);
		}
	}

	/**
	 * Recursively updates the world matrix of the given parent node.
	 * @param node Parent node
	 * @return World matrix
	 */
	private static Matrix parent(Node node) {
		// Stop at root
		if(node == null) {
			return Matrix.IDENTITY;
		}

		// Recursively update ancestors
		final LocalTransform transform = node.transform();
		transform.update(node);

		// Calculate world matrix
		return transform.world();
	}

	@Override
	public String toString() {
		return String.format("Transform[transform=%s world=%s]", transform, world);
	}
}
