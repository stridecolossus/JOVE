package org.sarge.jove.vulkan;

import java.util.Set;

public final class Vulkan {
	private static final Vulkan VULKAN;

	static {
		VULKAN = null; // TODO
	}

	public static Vulkan vulkan() {
		return VULKAN;
	}

	public Set<String> extensions() {
		return null;
	}

	public Set<Layer> layers() {
		return null;
	}
}
