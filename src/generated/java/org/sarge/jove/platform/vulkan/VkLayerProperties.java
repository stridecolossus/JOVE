package org.sarge.jove.platform.vulkan;

import java.lang.foreign.StructLayout;

import org.sarge.jove.lib.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkLayerProperties extends NativeStructure {
	public byte[] layerName = new byte[256];
	public int specVersion;
	public int implementationVersion;
	public byte[] description = new byte[256];

	@Override
	protected StructLayout layout() {
		// TODO
		return null;
	}
}
