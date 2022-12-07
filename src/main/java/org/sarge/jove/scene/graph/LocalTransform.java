package org.sarge.jove.scene.graph;

import static org.sarge.lib.util.Check.notNull;

import java.util.stream.Stream;

import org.sarge.jove.geometry.*;

/**
 * A <i>local transform</i> maintains the local transformation and world matrix of a node.
 * @author Sarge
 */
public class LocalTransform {
	private Transform transform = Matrix4.IDENTITY;
	private transient Matrix matrix;

	LocalTransform() {
	}

	/**
	 * @return Local transform
	 */
	public Transform transform() {
		return transform;
	}

	/**
	 * Sets the local transform.
	 * @param transform Local transform
	 */
	public void set(Transform transform) {
		this.transform = notNull(transform);
	}

	/**
	 * @return World matrix
	 * @throws IllegalStateException if this local transform has not been updated
	 * @see #update(Node)
	 */
	Matrix matrix() {
		if(matrix == null) throw new IllegalStateException("Local transform has not been updated: " + this);
		return matrix;
	}

	/**
	 * Updates the world matrix for the given node.
	 * @param node Node
	 */
	void update(Node node) {
		matrix = matrix(node);
	}

	/**
	 * Calculates the world matrix for the given node.
	 * @param node Node
	 * @return World matrix
	 */
	private Matrix matrix(Node node) {
		final Node parent = node.parent();
		final Matrix local = transform.matrix();
		if(parent == null) {
			return local;
		}
		else {
			final Matrix world = parent.transform().matrix();
    		if(local == Matrix4.IDENTITY) {
    			return world;
    		}
    		else {
    			return world.multiply(local);
    		}
    	}
	}

	@Override
	public int hashCode() {
		return transform.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj == this);
	}

	@Override
	public String toString() {
		return transform.toString();
	}

	/**
	 * The <i>update visitor</i> updates the local transforms of a scene graph.
	 */
	public static class UpdateVisitor {
		/**
		 * Recursively updates a scene graph.
		 * @param node Starting node
		 */
		public void update(Node node) {
			final var nodes = flatten(node);
			nodes.forEach(UpdateVisitor::visit);
		}

		/**
		 * Updates the given node.
		 */
		private static void visit(Node node) {
			node.transform().update(node);
		}

		/**
		 * @return Recursive stream for the given node and its children
		 */
		private static Stream<Node> flatten(Node node) {
			final var stream = Stream.of(node);
			if(node instanceof GroupNode group) {
				final var children = group.nodes().flatMap(UpdateVisitor::flatten);
				return Stream.concat(stream, children);
			}
			else {
				return stream;
			}
		}
	}
}
