package org.sarge.jove.scene.graph;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.model.Mesh;

/**
 * A <i>mesh node</i> is a renderable element of a scene graph.
 * TODO - link to root
 * @author Sarge
 */
public class MeshNode extends Node implements Renderable {
	private final Mesh mesh;
	private final Material mat;

	/**
	 * Constructor.
	 * @param parent	Parent node
	 * @param mesh		Mesh
	 * @param mat		Material
	 */
	public MeshNode(GroupNode parent, Mesh mesh, Material mat) {
		super(parent);
		this.mesh = notNull(mesh);
		this.mat = notNull(mat);
		attach();
	}

	private void attach() {
		final RootNode root = this.root();
		root.add(this);
	}

	/**
	 * @return Mesh
	 */
	public Mesh mesh() {
		return mesh;
	}

	/**
	 * @return Material
	 */
	public Material material() {
		return mat;
	}

	@Override
	public void detach() {
		final RootNode root = this.root();
		root.remove(this);
		super.detach();
	}

	@Override
	public int hashCode() {
		return Objects.hash(mesh, mat);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(mesh)
				.append(mat)
				.build();
	}
}
