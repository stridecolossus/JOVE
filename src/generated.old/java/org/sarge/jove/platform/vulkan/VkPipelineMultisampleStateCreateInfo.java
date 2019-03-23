package org.sarge.jove.platform.vulkan;

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
	"rasterizationSamples",
	"sampleShadingEnable",
	"minSampleShading",
	"pSampleMask",
	"alphaToCoverageEnable",
	"alphaToOneEnable"
})
public class VkPipelineMultisampleStateCreateInfo extends Structure {
	public static class ByValue extends VkPipelineMultisampleStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineMultisampleStateCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int rasterizationSamples = VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT.value();
	public boolean sampleShadingEnable;
	public float minSampleShading = 1;
	public int pSampleMask;
	public boolean alphaToCoverageEnable;
	public boolean alphaToOneEnable;

	// TODO - builder
}
