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
	"maxUpdateAfterBindDescriptorsInAllPools",
	"shaderUniformBufferArrayNonUniformIndexingNative",
	"shaderSampledImageArrayNonUniformIndexingNative",
	"shaderStorageBufferArrayNonUniformIndexingNative",
	"shaderStorageImageArrayNonUniformIndexingNative",
	"shaderInputAttachmentArrayNonUniformIndexingNative",
	"robustBufferAccessUpdateAfterBind",
	"quadDivergentImplicitLod",
	"maxPerStageDescriptorUpdateAfterBindSamplers",
	"maxPerStageDescriptorUpdateAfterBindUniformBuffers",
	"maxPerStageDescriptorUpdateAfterBindStorageBuffers",
	"maxPerStageDescriptorUpdateAfterBindSampledImages",
	"maxPerStageDescriptorUpdateAfterBindStorageImages",
	"maxPerStageDescriptorUpdateAfterBindInputAttachments",
	"maxPerStageUpdateAfterBindResources",
	"maxDescriptorSetUpdateAfterBindSamplers",
	"maxDescriptorSetUpdateAfterBindUniformBuffers",
	"maxDescriptorSetUpdateAfterBindUniformBuffersDynamic",
	"maxDescriptorSetUpdateAfterBindStorageBuffers",
	"maxDescriptorSetUpdateAfterBindStorageBuffersDynamic",
	"maxDescriptorSetUpdateAfterBindSampledImages",
	"maxDescriptorSetUpdateAfterBindStorageImages",
	"maxDescriptorSetUpdateAfterBindInputAttachments"
})
public class VkPhysicalDeviceDescriptorIndexingPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceDescriptorIndexingPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDescriptorIndexingPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DESCRIPTOR_INDEXING_PROPERTIES_EXT.value();
	public Pointer pNext;
	public int maxUpdateAfterBindDescriptorsInAllPools;
	public boolean shaderUniformBufferArrayNonUniformIndexingNative;
	public boolean shaderSampledImageArrayNonUniformIndexingNative;
	public boolean shaderStorageBufferArrayNonUniformIndexingNative;
	public boolean shaderStorageImageArrayNonUniformIndexingNative;
	public boolean shaderInputAttachmentArrayNonUniformIndexingNative;
	public boolean robustBufferAccessUpdateAfterBind;
	public boolean quadDivergentImplicitLod;
	public int maxPerStageDescriptorUpdateAfterBindSamplers;
	public int maxPerStageDescriptorUpdateAfterBindUniformBuffers;
	public int maxPerStageDescriptorUpdateAfterBindStorageBuffers;
	public int maxPerStageDescriptorUpdateAfterBindSampledImages;
	public int maxPerStageDescriptorUpdateAfterBindStorageImages;
	public int maxPerStageDescriptorUpdateAfterBindInputAttachments;
	public int maxPerStageUpdateAfterBindResources;
	public int maxDescriptorSetUpdateAfterBindSamplers;
	public int maxDescriptorSetUpdateAfterBindUniformBuffers;
	public int maxDescriptorSetUpdateAfterBindUniformBuffersDynamic;
	public int maxDescriptorSetUpdateAfterBindStorageBuffers;
	public int maxDescriptorSetUpdateAfterBindStorageBuffersDynamic;
	public int maxDescriptorSetUpdateAfterBindSampledImages;
	public int maxDescriptorSetUpdateAfterBindStorageImages;
	public int maxDescriptorSetUpdateAfterBindInputAttachments;
}
