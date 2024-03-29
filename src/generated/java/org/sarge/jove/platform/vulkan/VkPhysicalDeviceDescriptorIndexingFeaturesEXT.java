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
	"shaderInputAttachmentArrayDynamicIndexing",
	"shaderUniformTexelBufferArrayDynamicIndexing",
	"shaderStorageTexelBufferArrayDynamicIndexing",
	"shaderUniformBufferArrayNonUniformIndexing",
	"shaderSampledImageArrayNonUniformIndexing",
	"shaderStorageBufferArrayNonUniformIndexing",
	"shaderStorageImageArrayNonUniformIndexing",
	"shaderInputAttachmentArrayNonUniformIndexing",
	"shaderUniformTexelBufferArrayNonUniformIndexing",
	"shaderStorageTexelBufferArrayNonUniformIndexing",
	"descriptorBindingUniformBufferUpdateAfterBind",
	"descriptorBindingSampledImageUpdateAfterBind",
	"descriptorBindingStorageImageUpdateAfterBind",
	"descriptorBindingStorageBufferUpdateAfterBind",
	"descriptorBindingUniformTexelBufferUpdateAfterBind",
	"descriptorBindingStorageTexelBufferUpdateAfterBind",
	"descriptorBindingUpdateUnusedWhilePending",
	"descriptorBindingPartiallyBound",
	"descriptorBindingVariableDescriptorCount",
	"runtimeDescriptorArray"
})
public class VkPhysicalDeviceDescriptorIndexingFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceDescriptorIndexingFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceDescriptorIndexingFeaturesEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_DESCRIPTOR_INDEXING_FEATURES_EXT;
	public Pointer pNext;
	public boolean shaderInputAttachmentArrayDynamicIndexing;
	public boolean shaderUniformTexelBufferArrayDynamicIndexing;
	public boolean shaderStorageTexelBufferArrayDynamicIndexing;
	public boolean shaderUniformBufferArrayNonUniformIndexing;
	public boolean shaderSampledImageArrayNonUniformIndexing;
	public boolean shaderStorageBufferArrayNonUniformIndexing;
	public boolean shaderStorageImageArrayNonUniformIndexing;
	public boolean shaderInputAttachmentArrayNonUniformIndexing;
	public boolean shaderUniformTexelBufferArrayNonUniformIndexing;
	public boolean shaderStorageTexelBufferArrayNonUniformIndexing;
	public boolean descriptorBindingUniformBufferUpdateAfterBind;
	public boolean descriptorBindingSampledImageUpdateAfterBind;
	public boolean descriptorBindingStorageImageUpdateAfterBind;
	public boolean descriptorBindingStorageBufferUpdateAfterBind;
	public boolean descriptorBindingUniformTexelBufferUpdateAfterBind;
	public boolean descriptorBindingStorageTexelBufferUpdateAfterBind;
	public boolean descriptorBindingUpdateUnusedWhilePending;
	public boolean descriptorBindingPartiallyBound;
	public boolean descriptorBindingVariableDescriptorCount;
	public boolean runtimeDescriptorArray;
}
