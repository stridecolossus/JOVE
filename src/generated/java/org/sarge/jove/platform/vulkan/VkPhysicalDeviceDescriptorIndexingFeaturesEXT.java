package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;
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
	public VulkanBoolean shaderInputAttachmentArrayDynamicIndexing;
	public VulkanBoolean shaderUniformTexelBufferArrayDynamicIndexing;
	public VulkanBoolean shaderStorageTexelBufferArrayDynamicIndexing;
	public VulkanBoolean shaderUniformBufferArrayNonUniformIndexing;
	public VulkanBoolean shaderSampledImageArrayNonUniformIndexing;
	public VulkanBoolean shaderStorageBufferArrayNonUniformIndexing;
	public VulkanBoolean shaderStorageImageArrayNonUniformIndexing;
	public VulkanBoolean shaderInputAttachmentArrayNonUniformIndexing;
	public VulkanBoolean shaderUniformTexelBufferArrayNonUniformIndexing;
	public VulkanBoolean shaderStorageTexelBufferArrayNonUniformIndexing;
	public VulkanBoolean descriptorBindingUniformBufferUpdateAfterBind;
	public VulkanBoolean descriptorBindingSampledImageUpdateAfterBind;
	public VulkanBoolean descriptorBindingStorageImageUpdateAfterBind;
	public VulkanBoolean descriptorBindingStorageBufferUpdateAfterBind;
	public VulkanBoolean descriptorBindingUniformTexelBufferUpdateAfterBind;
	public VulkanBoolean descriptorBindingStorageTexelBufferUpdateAfterBind;
	public VulkanBoolean descriptorBindingUpdateUnusedWhilePending;
	public VulkanBoolean descriptorBindingPartiallyBound;
	public VulkanBoolean descriptorBindingVariableDescriptorCount;
	public VulkanBoolean runtimeDescriptorArray;
}
