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
	"representativeFragmentTestEnable"
})
public class VkPipelineRepresentativeFragmentTestStateCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkPipelineRepresentativeFragmentTestStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRepresentativeFragmentTestStateCreateInfoNV implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PIPELINE_REPRESENTATIVE_FRAGMENT_TEST_STATE_CREATE_INFO_NV;
	public Pointer pNext;
	public boolean representativeFragmentTestEnable;
}
