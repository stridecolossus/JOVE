package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Transform;

/**
 * A <i>local transform</i> maintains the world matrix of a node.
 */
public final class LocalTransform {
	/**
	 * Node visitor that updates the world matrix of a scene-graph.
	 */
	public static class Visitor implements Node.Visitor {
		@Override
		public boolean visit(Node node) {
			/*
			final LocalTransform local = node.transform();
			if((local.matrix == null) || local.transform.isDirty()) {
				final Node parent = node.parent();
				if(parent == null) {
					local.matrix = local.transform.matrix();
				}
				else {
					local.matrix =
				}


				if(parent != null) {
					local.update(parent.transform().matrix());
				}
			}
			*/

			// TODO
			// - root node transform to avoid parent check?
			// - flag from dirty parent?
			final LocalTransform local = node.transform();
			if(node.parent() == null) {
				local.matrix = local.transform.matrix();
			}
			else {
				local.update(node.parent().transform().matrix);
			}
			return true;
		}
	}

	/**
	 * Creates an empty (or identity) transform.
	 * @return Empty transform
	 * @see Matrix#IDENTITY
	 */
	public static LocalTransform none() {
		return new LocalTransform(Matrix.IDENTITY);
	}

	private final Transform transform;

	private Matrix matrix;

	/**
	 * Constructor.
	 * @param transform Transform
	 */
	public LocalTransform(Transform transform) {
		this.transform = notNull(transform);
	}

	/**
	 * @return Transform
	 */
	public Transform transform() {
		return transform;
	}

	/**
	 * @return World matrix
	 */
	public Matrix matrix() {
		return matrix;
	}

	/**
	 * Updates the world matrix of this transform.
	 * @param world Parent world matrix
	 */
	void update(Matrix world) {
		if(transform == Matrix.IDENTITY) {
			matrix = world;
		}
		else {
			matrix = world.multiply(transform.matrix());
		}
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
