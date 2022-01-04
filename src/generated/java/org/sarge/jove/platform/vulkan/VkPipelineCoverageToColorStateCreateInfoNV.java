package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

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
	"coverageToColorEnable",
	"coverageToColorLocation"
})
public class VkPipelineCoverageToColorStateCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkPipelineCoverageToColorStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineCoverageToColorStateCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.PIPELINE_COVERAGE_TO_COLOR_STATE_CREATE_INFO_NV;
	public Pointer pNext;
	public int flags;
	public VulkanBoolean coverageToColorEnable;
	public int coverageToColorLocation;
}
