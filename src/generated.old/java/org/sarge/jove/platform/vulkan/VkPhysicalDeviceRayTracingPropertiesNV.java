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
	"shaderGroupHandleSize",
	"maxRecursionDepth",
	"maxShaderGroupStride",
	"shaderGroupBaseAlignment",
	"maxGeometryCount",
	"maxInstanceCount",
	"maxTriangleCount",
	"maxDescriptorSetAccelerationStructures"
})
public class VkPhysicalDeviceRayTracingPropertiesNV extends Structure {
	public static class ByValue extends VkPhysicalDeviceRayTracingPropertiesNV implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceRayTracingPropertiesNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_RAY_TRACING_PROPERTIES_NV.value();
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
