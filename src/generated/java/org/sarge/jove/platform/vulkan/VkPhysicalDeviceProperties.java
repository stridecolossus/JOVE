package org.sarge.jove.platform.vulkan;

import java.lang.foreign.StructLayout;

import org.sarge.jove.lib.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPhysicalDeviceProperties extends NativeStructure {
	public int apiVersion;
	public int driverVersion;
	public int vendorID;
	public int deviceID;
	public VkPhysicalDeviceType deviceType;
	public byte[] deviceName = new byte[256];
	public byte[] pipelineCacheUUID = new byte[16];
	public VkPhysicalDeviceLimits limits;
	public VkPhysicalDeviceSparseProperties sparseProperties;

	@Override
	protected StructLayout layout() {
		// TODO
		return null;
	}
}
