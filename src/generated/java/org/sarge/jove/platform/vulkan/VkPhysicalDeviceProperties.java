package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

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
	public String deviceName;
	public byte[] pipelineCacheUUID;
	public VkPhysicalDeviceLimits limits;
	public VkPhysicalDeviceSparseProperties sparseProperties;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
            JAVA_INT.withName("apiVersion"),
            JAVA_INT.withName("driverVersion"),
            JAVA_INT.withName("vendorID"),
            JAVA_INT.withName("deviceID"),
            JAVA_INT.withName("deviceType"),
            MemoryLayout.sequenceLayout(256, JAVA_BYTE).withName("deviceName"),
            MemoryLayout.sequenceLayout(16, JAVA_BYTE).withName("pipelineCacheUUID"),
            PADDING,
            new VkPhysicalDeviceLimits().layout().withName("limits"),
            new VkPhysicalDeviceSparseProperties().layout().withName("sparseProperties"),
            PADDING
        );
	}
}
