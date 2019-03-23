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
public class VkObjectTableCreateInfoNVX extends Structure {
	public static class ByValue extends VkObjectTableCreateInfoNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTableCreateInfoNVX implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_OBJECT_TABLE_CREATE_INFO_NVX.value();
	public Pointer pNext;
	public int objectCount;
	public int pObjectEntryTypes;
	public int pObjectEntryCounts;
	public int pObjectEntryUsageFlags;
	public int maxUniformBuffersPerDescriptor;
	public int maxStorageBuffersPerDescriptor;
	public int maxStorageImagesPerDescriptor;
	public int maxSampledImagesPerDescriptor;
	public int maxPipelineLayouts;
}
