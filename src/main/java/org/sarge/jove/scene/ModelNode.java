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
public class ModelNode extends AbstractNode implements Renderable {
	private final Model model;
	private Material mat;

	/**
	 * Constructor.
	 * @param model Model
	 */
	public ModelNode(Model model) {
		this.model = notNull(model);
	}

	/**
	 * @return Renderable model
	 */
	public Model model() {
		return model;
	}

	/**
	 * @return Material
	 */
	public Material material() {
		// TODO - walk ancestors
		//if(mat == null) throw new IllegalStateException();
		return mat;
	}

	/**
	 * Sets the material for this node.
	 * @param mat Material or {@code null} for none
	 */
	public void material(Material mat) {
		this.mat = mat;
	}

	@Override
	public Stream<SceneGraph> nodes() {
		return Stream.of(this);
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), model, mat);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof ModelNode that) &&
				(this.model == that.model) &&
				Objects.equals(this.mat, that.mat) &&		// TODO
				super.isEqual(that);
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
