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
	"flags",
	"coverageModulationMode",
	"coverageModulationTableEnable",
	"coverageModulationTableCount",
	"pCoverageModulationTable"
})
public class VkPipelineCoverageModulationStateCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkPipelineCoverageModulationStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineCoverageModulationStateCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_COVERAGE_MODULATION_STATE_CREATE_INFO_NV;
	public Pointer pNext;
	public int flags;
	public VkCoverageModulationModeNV coverageModulationMode;
	public VulkanBoolean coverageModulationTableEnable;
	public int coverageModulationTableCount;
	public Pointer pCoverageModulationTable;
}
