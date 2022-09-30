package org.sarge.jove.scene;

import java.util.*;
import java.util.function.Consumer;

/**
 * A <i>render queue</i>
 * TODO
 * defines the render order for a set of geometry.
 *
 * flattened representation of a scene
 * mutable
 * {@link GroupNode#add(AbstractNode)}
 *
 *
 * @author Sarge
 */
public class RenderQueue {
	private final Map<Material, TextureGroup> groups = new HashMap<>();

	/**
	 * Renders nodes in this queue.
	 * @param renderer Renderer
	 */
	public void render(Consumer<Renderable> renderer) {
		for(var entry : groups.entrySet()) {
			final Material mat = entry.getKey();
			final TextureGroup g = entry.getValue();
			renderer.accept(mat);
			g.render(renderer);
		}
	}

	/**
	 * Adds a node to this queue.
	 * @param node Node to add
	 */
	void add(ModelNode node) {
		final Material mat = node.material().material();
		final TextureGroup g = groups.computeIfAbsent(mat, __ -> new TextureGroup());
		g.add(node);
	}

	/**
	 * Removes a node from this queue.
	 * @param node Node to remove
	 */
	void remove(ModelNode node) {
		final Material mat = node.material().material();
		final TextureGroup g = groups.get(mat);
		g.remove(node);
		if(g.groups.isEmpty()) {
			groups.remove(mat);
		}
	}

	/**
	 *
	 */
	private static class TextureGroup {
		private final Map<String, List<Renderable>> groups = new HashMap<>();

		private void render(Consumer<Renderable> renderer) {
			for(var entry : groups.entrySet()) {
				final String texture = entry.getKey();
				// TODO - texture
				for(Renderable n : entry.getValue()) {
					renderer.accept(n);
				}
			}
		}

		private void add(ModelNode node) {
			final String key = "texture"; // TODO
			final List<Renderable> list = groups.computeIfAbsent(key, __ -> new ArrayList<>());
			assert !list.contains(node);
			list.add(node);
		}

		private void remove(ModelNode node) {
			final String key = "texture"; // TODO
			final List<Renderable> list = groups.get(key);
			assert list.contains(node);
			list.remove(node);
			if(list.isEmpty()) {
				groups.remove(key);
			}
		}
	}
}
