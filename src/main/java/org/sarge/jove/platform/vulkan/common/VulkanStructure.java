package org.sarge.jove.platform.vulkan.common;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

public abstract class VulkanStructure implements NativeStructure {
	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
		);
	}
}
// TODO - temporary workaround
