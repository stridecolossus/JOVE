package org.sarge.jove.platform.vulkan;

import java.lang.foreign.StructLayout;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkExtensionProperties extends NativeStructure {
	public byte[] extensionName = new byte[256];
	public int specVersion;

	@Override
	protected StructLayout layout() {
		// TODO
		return null;
	}
}
