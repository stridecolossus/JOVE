package org.sarge.jove.scene.graph;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.model.Mesh;

/**
 * A <i>model node</i> is used to render a {@link Mesh} in the scene.
 * TODO - doc linear data structure
 * @author Sarge
 */
public final class MeshNode extends LeafNode {
	private final Mesh model;

	/**
	 * Constructor.
	 * @param mesh Renderable mesh
	 */
	public MeshNode(Mesh mesh) {
		this.model = notNull(mesh);
	}

	/**
	 * Copy constructor.
	 * @param node Model node to copy
	 */
	protected MeshNode(MeshNode node) {
		super(node);
		this.model = node.model;
	}

	/**
	 * @return Model
	 */
	public Mesh model() {
		return model;
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
	public MeshNode copy() {
		return new MeshNode(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), model);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(model)
				.build();
	}
}
