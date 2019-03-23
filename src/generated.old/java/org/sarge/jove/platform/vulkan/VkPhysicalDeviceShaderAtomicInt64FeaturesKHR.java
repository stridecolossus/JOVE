package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkPhysicalDeviceShaderAtomicInt64FeaturesKHR extends Structure {
	public static class ByValue extends VkPhysicalDeviceShaderAtomicInt64FeaturesKHR implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShaderAtomicInt64FeaturesKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_ATOMIC_INT_64_FEATURES_KHR.value();
	public Pointer pNext;
	public boolean shaderBufferInt64Atomics;
	public boolean shaderSharedInt64Atomics;
}
