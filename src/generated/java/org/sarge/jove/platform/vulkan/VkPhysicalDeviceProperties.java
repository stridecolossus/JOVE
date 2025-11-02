package org.sarge.jove.platform.vulkan;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPhysicalDeviceProperties implements NativeStructure {
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
	public StructLayout layout() {
		//throw new UnsupportedOperationException("TODO - arrays!!!");
		// TODO
		return MemoryLayout.structLayout();
	}
}
