package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"rasterizationSamples",
	"sampleShadingEnable",
	"minSampleShading",
	"pSampleMask",
	"alphaToCoverageEnable",
	"alphaToOneEnable"
})
public class VkPipelineMultisampleStateCreateInfo extends VulkanStructure implements ByReference {
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public int rasterizationSamples = VkSampleCountFlag.VK_SAMPLE_COUNT_1.value();
	public VulkanBoolean sampleShadingEnable = VulkanBoolean.FALSE;
	public float minSampleShading = 1;
	public Pointer pSampleMask;
	public VulkanBoolean alphaToCoverageEnable = VulkanBoolean.FALSE;
	public VulkanBoolean alphaToOneEnable = VulkanBoolean.FALSE;
	// TODO - fiddled values above!!!
}
