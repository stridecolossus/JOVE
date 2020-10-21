package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
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
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_ATOMIC_INT_64_FEATURES_KHR;
	public Pointer pNext;
	public VulkanBoolean shaderBufferInt64Atomics;
	public VulkanBoolean shaderSharedInt64Atomics;
}
