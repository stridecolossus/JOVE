package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"mapEntryCount",
	"pMapEntries",
	"dataSize",
	"pData"
})
public class VkSpecializationInfo extends VulkanStructure {
	public static class ByValue extends VkSpecializationInfo implements Structure.ByValue { }
	public static class ByReference extends VkSpecializationInfo implements Structure.ByReference { }
	
	public int mapEntryCount;
	public Pointer pMapEntries;
	public long dataSize;
	public Pointer pData;
}
