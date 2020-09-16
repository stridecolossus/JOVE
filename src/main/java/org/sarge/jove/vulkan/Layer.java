package org.sarge.jove.vulkan;

import java.util.Set;

import org.sarge.lib.util.Check;

public record Layer(String name, int version) implements Comparable<Layer> {
	public static boolean contains(Set<Layer> layers, Layer layer) {
		return false;
	}

	public Layer {
		Check.notEmpty(name);
		Check.oneOrMore(version);
	}

	@Override
	public int compareTo(Layer that) {
		return 0; // TODO
	}
}
