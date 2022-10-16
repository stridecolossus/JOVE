package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.model.Mesh;

/**
 * A <i>model node</i> is used to render a {@link Mesh} in the scene.
 * @author Sarge
 */
public final class ModelNode extends LeafNode {
	private final RenderQueue queue;
	private final Mesh mesh;

	/**
	 * Constructor.
	 * @param queue		Render queue for this node
	 * @param mesh 		Renderable mesh
	 */
	public ModelNode(RenderQueue queue, Mesh mesh) {
		this.queue = notNull(queue);
		this.mesh = notNull(mesh);
	}

	/**
	 * Copy constructor.
	 * @param node Model node to copy
	 */
	protected ModelNode(ModelNode node) {
		super(node);
		this.queue = node.queue;
		this.mesh = node.mesh;
	}

	/**
	 * @return Render queue for this node
	 */
	public RenderQueue queue() {
		return queue;
	}
	// TODO - should be property of material?

	/**
	 * @return Renderable mesh
	 */
	public Mesh mesh() {
		return mesh;
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
				.append(queue)
				.append(mesh)
				.build();
	}
}
