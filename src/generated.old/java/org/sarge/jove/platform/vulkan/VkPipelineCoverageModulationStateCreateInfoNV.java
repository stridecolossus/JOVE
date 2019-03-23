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
	"flags",
	"coverageModulationMode",
	"coverageModulationTableEnable",
	"coverageModulationTableCount",
	"pCoverageModulationTable"
})
public class VkPipelineCoverageModulationStateCreateInfoNV extends Structure {
	public static class ByValue extends VkPipelineCoverageModulationStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineCoverageModulationStateCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_COVERAGE_MODULATION_STATE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public int flags;
	public int coverageModulationMode;
	public boolean coverageModulationTableEnable;
	public int coverageModulationTableCount;
	public float[] pCoverageModulationTable;
}
