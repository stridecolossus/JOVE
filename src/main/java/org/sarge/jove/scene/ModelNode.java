package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.model.Model;

/**
 *
 * @author Sarge
 */
public class ModelNode extends Node implements Renderable {
	private final Model model;
	private Material mat;

	/**
	 * Constructor.
	 * @param model Model
	 */
	public ModelNode(Model model) {
		this.model = notNull(model);
	}

	@Override
	public Model model() {
		return model;
	}

	@Override
	public Optional<Material> material() {
		return Optional.ofNullable(mat);
	}

	/**
	 * Sets the material for this node.
	 * @param mat Material or {@code null} for none
	 */
	public void material(Material mat) {
		this.mat = mat;
	}

	@Override
	public Stream<Renderable> render() {
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
				Objects.equals(this.mat, that.mat) &&
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
