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
public class VkPhysicalDeviceConservativeRasterizationPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceConservativeRasterizationPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceConservativeRasterizationPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_CONSERVATIVE_RASTERIZATION_PROPERTIES_EXT.value();
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
