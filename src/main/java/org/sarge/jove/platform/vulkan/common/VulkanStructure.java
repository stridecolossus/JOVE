package org.sarge.jove.platform.vulkan.common;

import java.lang.foreign.StructLayout;

import org.sarge.jove.foreign.NativeStructure;

public abstract class VulkanStructure extends NativeStructure {
	@Override
	protected StructLayout layout() {
		throw new UnsupportedOperationException();
	}
}
// TODO - temporary workaround

