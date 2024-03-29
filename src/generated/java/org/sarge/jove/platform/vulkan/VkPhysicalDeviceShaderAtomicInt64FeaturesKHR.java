package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"shaderBufferInt64Atomics",
	"shaderSharedInt64Atomics"
})
public class VkPhysicalDeviceShaderAtomicInt64FeaturesKHR extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceShaderAtomicInt64FeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShaderAtomicInt64FeaturesKHR implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_SHADER_ATOMIC_INT64_FEATURES_KHR;
	public Pointer pNext;
	public boolean shaderBufferInt64Atomics;
	public boolean shaderSharedInt64Atomics;
}
