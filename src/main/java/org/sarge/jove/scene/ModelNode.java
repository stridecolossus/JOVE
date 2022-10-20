package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.model.Mesh;

/**
 * A <i>model node</i> is used to render a {@link Mesh} in the scene.
 * TODO - doc linear data structure
 * @author Sarge
 */
public final class ModelNode extends LeafNode {
	private final Mesh mesh;

	/**
	 * Constructor.
	 * @param mesh Renderable mesh
	 */
	public ModelNode(Mesh mesh) {
		this.mesh = notNull(mesh);
	}

	/**
	 * Copy constructor.
	 * @param node Model node to copy
	 */
	protected ModelNode(ModelNode node) {
		super(node);
		this.mesh = node.mesh;
	}

	/**
	 * @return Renderable mesh
	 */
	public Mesh mesh() {
		return mesh;
	}

	@Override
	protected void attach(Node parent) {
		super.attach(parent);
//		queue.add(this);
	}

	@Override
	protected void detach() {
		super.detach();
//		queue.remove(this);
	}

	@Override
	public ModelNode copy() {
		return new ModelNode(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), mesh);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(mesh)
				.build();
	}
}
