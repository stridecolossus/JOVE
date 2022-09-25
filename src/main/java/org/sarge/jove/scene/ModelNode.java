package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.model.Model;

/**
 * A <i>model node</i> represents a renderable model within the scene.
 * @author Sarge
 */
public final class ModelNode extends AbstractNode implements Renderable {
	private final Model model;

	/**
	 * Constructor.
	 * @param model Model
	 */
	public ModelNode(Model model) {
		this.model = notNull(model);
	}

	/**
	 * @return Model
	 */
	public Model model() {
		return model;
	}

	@Override
	public Stream<AbstractNode> nodes() {
		return Stream.of(this);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

	@Override
	protected void attach(AbstractNode parent) {
		super.attach(parent);
		// TODO - add material/DS
	}

	@Override
	protected void detach() {
		super.detach();
		material().queue().remove(this);
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
				.append(material())
				.build();
	}
}
