package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
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
	public VkStructureType sType = VkStructureType.PIPELINE_MULTISAMPLE_STATE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public VkSampleCount rasterizationSamples;
	public VulkanBoolean sampleShadingEnable;
	public float minSampleShading;
	public Pointer pSampleMask;
	public VulkanBoolean alphaToCoverageEnable;
	public VulkanBoolean alphaToOneEnable;
}
