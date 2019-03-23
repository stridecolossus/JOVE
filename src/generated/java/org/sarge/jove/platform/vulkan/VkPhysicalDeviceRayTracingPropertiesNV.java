package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
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
	"shaderGroupHandleSize",
	"maxRecursionDepth",
	"maxShaderGroupStride",
	"shaderGroupBaseAlignment",
	"maxGeometryCount",
	"maxInstanceCount",
	"maxTriangleCount",
	"maxDescriptorSetAccelerationStructures"
})
public class VkPhysicalDeviceRayTracingPropertiesNV extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceRayTracingPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceRayTracingPropertiesNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_RAY_TRACING_PROPERTIES_NV;
	public Pointer pNext;
	public int shaderGroupHandleSize;
	public int maxRecursionDepth;
	public int maxShaderGroupStride;
	public int shaderGroupBaseAlignment;
	public long maxGeometryCount;
	public long maxInstanceCount;
	public long maxTriangleCount;
	public int maxDescriptorSetAccelerationStructures;
}
