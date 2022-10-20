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

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_CONSERVATIVE_RASTERIZATION_PROPERTIES_EXT;
	public Pointer pNext;
	public float primitiveOverestimationSize;
	public float maxExtraPrimitiveOverestimationSize;
	public float extraPrimitiveOverestimationSizeGranularity;
	public boolean primitiveUnderestimation;
	public boolean conservativePointAndLineRasterization;
	public boolean degenerateTrianglesRasterized;
	public boolean degenerateLinesRasterized;
	public boolean fullyCoveredFragmentShaderInputVariable;
	public boolean conservativeRasterizationPostDepthCoverage;
}
