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
	"shaderEngineCount",
	"shaderArraysPerEngineCount",
	"computeUnitsPerShaderArray",
	"simdPerComputeUnit",
	"wavefrontsPerSimd",
	"wavefrontSize",
	"sgprsPerSimd",
	"minSgprAllocation",
	"maxSgprAllocation",
	"sgprAllocationGranularity",
	"vgprsPerSimd",
	"minVgprAllocation",
	"maxVgprAllocation",
	"vgprAllocationGranularity"
})
public class VkPhysicalDeviceShaderCorePropertiesAMD extends Structure {
	public static class ByValue extends VkPhysicalDeviceShaderCorePropertiesAMD implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceShaderCorePropertiesAMD implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_CORE_PROPERTIES_AMD.value();
	public Pointer pNext;
	public int shaderEngineCount;
	public int shaderArraysPerEngineCount;
	public int computeUnitsPerShaderArray;
	public int simdPerComputeUnit;
	public int wavefrontsPerSimd;
	public int wavefrontSize;
	public int sgprsPerSimd;
	public int minSgprAllocation;
	public int maxSgprAllocation;
	public int sgprAllocationGranularity;
	public int vgprsPerSimd;
	public int minVgprAllocation;
	public int maxVgprAllocation;
	public int vgprAllocationGranularity;
}
