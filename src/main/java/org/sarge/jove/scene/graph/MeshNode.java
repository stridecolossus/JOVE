package org.sarge.jove.scene.graph;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

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
		this.mesh = requireNonNull(mesh);
		this.mat = requireNonNull(mat);
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
}
