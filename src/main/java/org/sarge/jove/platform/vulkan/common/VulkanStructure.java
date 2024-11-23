package org.sarge.jove.platform.vulkan.common;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

public abstract class VulkanStructure extends NativeStructure {
	@Override
	protected StructLayout layout() {
		return MemoryLayout.structLayout(
		);
	}
}
// TODO - temporary workaround

