package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"apiVersion",
	"driverVersion",
	"vendorID",
	"deviceID",
	"deviceType",
	"deviceName",
	"pipelineCacheUUID",
	"limits",
	"sparseProperties"
})
public class VkPhysicalDeviceProperties extends Structure {
	public static class ByValue extends VkPhysicalDeviceProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceProperties implements Structure.ByReference { }
	
	public int apiVersion;
	public int driverVersion;
	public int vendorID;
	public int deviceID;
	public int deviceType;
	public final byte[] deviceName = new byte[256];
	public final byte[] pipelineCacheUUID = new byte[16];
	public VkPhysicalDeviceLimits limits;
	public VkPhysicalDeviceSparseProperties sparseProperties;
}
