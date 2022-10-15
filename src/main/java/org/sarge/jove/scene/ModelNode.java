package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.model.Model;

/**
 * A <i>model node</i> represents a renderable model within the scene.
 * @author Sarge
 */
public final class ModelNode extends LeafNode {
	private final RenderQueue queue;
	private final Model model; // TODO - geometry (?)

	/**
	 * Constructor.
	 * @param model Model
	 */
	public ModelNode(RenderQueue queue, Model model) {
		this.queue = notNull(queue);
		this.model = notNull(model);
	}

	/**
	 * @return Render queue for this node
	 */
	public RenderQueue queue() {
		return queue;
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
		queue.add(this);
	}

	@Override
	protected void detach() {
		super.detach();
		queue.remove(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), model);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(queue)
				.append(model)
				.build();
	}
}
