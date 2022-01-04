package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

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
	"objectCount",
	"pObjectEntryTypes",
	"pObjectEntryCounts",
	"pObjectEntryUsageFlags",
	"maxUniformBuffersPerDescriptor",
	"maxStorageBuffersPerDescriptor",
	"maxStorageImagesPerDescriptor",
	"maxSampledImagesPerDescriptor",
	"maxPipelineLayouts"
})
public class VkObjectTableCreateInfoNVX extends VulkanStructure {
	public static class ByValue extends VkObjectTableCreateInfoNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTableCreateInfoNVX implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.OBJECT_TABLE_CREATE_INFO_NVX;
	public Pointer pNext;
	public int objectCount;
	public Pointer pObjectEntryTypes;
	public Pointer pObjectEntryCounts;
	public Pointer pObjectEntryUsageFlags;
	public int maxUniformBuffersPerDescriptor;
	public int maxStorageBuffersPerDescriptor;
	public int maxStorageImagesPerDescriptor;
	public int maxSampledImagesPerDescriptor;
	public int maxPipelineLayouts;
}
