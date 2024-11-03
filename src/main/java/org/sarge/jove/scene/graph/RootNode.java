package org.sarge.jove.scene.graph;

import java.util.*;
import java.util.stream.Stream;

/**
 * The <i>root node</i>
 * TODO
 * @author Sarge
 */
public class RootNode extends GroupNode {
	private final Map<Material, List<MeshNode>> nodes = new HashMap<>();

	/**
	 * Constructor.
	 */
	public RootNode() {
	}

	@Override
	public RootNode root() {
		return this;
	}

	// TODO
	public Stream<Renderable> render() {
		// - order by states
		// - include states & nodes
		return null;
	}

	/**
	 * Adds a renderable node
	 * TODO
	 * @param node Renderable node
	 */
	void add(MeshNode node) {
		final var list = nodes.computeIfAbsent(node.material(), __ -> new ArrayList<>());
		assert !list.contains(node);
		list.add(node);
	}

	/**
	 * Removes a renderable node.
	 * @param node Renderable node
	 */
	void remove(MeshNode node) {
		final Material mat = node.material();
		final var list = nodes.get(mat);
		assert list != null;
		assert list.contains(node);
 		list.remove(node);
 		if(list.isEmpty()) {
 			nodes.remove(mat);
 		}
	}

	@Override
	public void clear() {
		super.clear();
		nodes.clear();
	}
}
