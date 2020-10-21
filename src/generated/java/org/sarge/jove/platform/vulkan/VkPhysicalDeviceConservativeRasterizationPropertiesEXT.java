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
	"primitiveOverestimationSize",
	"maxExtraPrimitiveOverestimationSize",
	"extraPrimitiveOverestimationSizeGranularity",
	"primitiveUnderestimation",
	"conservativePointAndLineRasterization",
	"degenerateTrianglesRasterized",
	"degenerateLinesRasterized",
	"fullyCoveredFragmentShaderInputVariable",
	"conservativeRasterizationPostDepthCoverage"
})
public class VkPhysicalDeviceConservativeRasterizationPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceConservativeRasterizationPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceConservativeRasterizationPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_CONSERVATIVE_RASTERIZATION_PROPERTIES_EXT;
	public Pointer pNext;
	public float primitiveOverestimationSize;
	public float maxExtraPrimitiveOverestimationSize;
	public float extraPrimitiveOverestimationSizeGranularity;
	public VulkanBoolean primitiveUnderestimation;
	public VulkanBoolean conservativePointAndLineRasterization;
	public VulkanBoolean degenerateTrianglesRasterized;
	public VulkanBoolean degenerateLinesRasterized;
	public VulkanBoolean fullyCoveredFragmentShaderInputVariable;
	public VulkanBoolean conservativeRasterizationPostDepthCoverage;
}
