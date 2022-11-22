package org.sarge.jove.scene.graph;

import java.util.*;
import java.util.function.Consumer;

/**
 * A <i>render queue</i>
 * TODO
 * defines the render order for a set of geometry.
 *
 * flattened representation of a scene
 * mutable
 *
 *
 * @author Sarge
 */
public class RenderQueue {
	private final Map<Material, Map<Renderable, List<MeshNode>>> queues = new HashMap<>();

	/**
	 * Adds a node to this queue.
	 * @param node Node to add
	 */
	void add(MeshNode node) {
		final Material mat = node.material().material();
		if(mat == Material.NONE) {
			return;
		}

		final var textures = queues.computeIfAbsent(mat, __ -> new HashMap<>());
		final Renderable tex = mat.texture();
		textures.computeIfAbsent(tex, __ -> new ArrayList<>()).add(node);
	}

	/**
	 * Removes a node from this queue.
	 * @param node Node to remove
	 */
	void remove(MeshNode node) {
		final Material mat = node.material().material();
		final var materials = queues.get(mat);
		if(materials == null) {
			return;
		}

		final var textures = queues.remove(mat);
		final var nodes = textures.get(mat.texture());
		nodes.remove(node);
	}

	/**
	 * Renders this queue.
	 * @param renderer Target renderer
	 */
	public void render(Consumer<Renderable> renderer) {
		for(var entry : queues.entrySet()) {
			final Material mat = entry.getKey();
			renderer.accept(mat);

			for(var e : entry.getValue().entrySet()) {
				renderer.accept(e.getKey());

				for(MeshNode n : e.getValue()) {
					// TODO - renderer.accept(n.model());
				}
			}
		}
	}
}
		// TODO - Consumer<Renderable>
		// - grouped by pipeline state changes:
		// - material
		// - pipeline
		// - descriptor set?
		// - texture? => Texture interface?
		// - others?
