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
	"representativeFragmentTestEnable"
})
public class VkPipelineRepresentativeFragmentTestStateCreateInfoNV extends VulkanStructure {
	public static class ByValue extends VkPipelineRepresentativeFragmentTestStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineRepresentativeFragmentTestStateCreateInfoNV implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_REPRESENTATIVE_FRAGMENT_TEST_STATE_CREATE_INFO_NV;
	public Pointer pNext;
	public VulkanBoolean representativeFragmentTestEnable;
}
