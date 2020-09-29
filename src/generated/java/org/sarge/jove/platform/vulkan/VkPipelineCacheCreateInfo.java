package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

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
	"initialDataSize",
	"pInitialData"
})
public class VkPipelineCacheCreateInfo extends VulkanStructure {
	public static class ByValue extends VkPipelineCacheCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineCacheCreateInfo implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public long initialDataSize;
	public Pointer pInitialData;
}
