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
	"coverageToColorEnable",
	"coverageToColorLocation"
})
public class VkPipelineCoverageToColorStateCreateInfoNV extends Structure {
	public static class ByValue extends VkPipelineCoverageToColorStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineCoverageToColorStateCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_COVERAGE_TO_COLOR_STATE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public int flags;
	public boolean coverageToColorEnable;
	public int coverageToColorLocation;
}
