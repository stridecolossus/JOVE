package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.model.Model;

/**
 * A <i>model node</i> is used to render a {@link Mesh} in the scene.
 * TODO - doc linear data structure
 * @author Sarge
 */
public final class ModelNode extends LeafNode {
	private final Model model;

	/**
	 * Constructor.
	 * @param mesh Renderable mesh
	 */
	public ModelNode(Model mesh) {
		this.model = notNull(mesh);
	}

	/**
	 * Copy constructor.
	 * @param node Model node to copy
	 */
	protected ModelNode(ModelNode node) {
		super(node);
		this.model = node.model;
	}

	/**
	 * @return Model
	 */
	public Model model() {
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
	public ModelNode copy() {
		return new ModelNode(this);
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
