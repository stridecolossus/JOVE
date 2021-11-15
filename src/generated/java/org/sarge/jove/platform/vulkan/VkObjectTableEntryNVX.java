package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"type",
	"flags"
})
public class VkObjectTableEntryNVX extends VulkanStructure {
	public static class ByValue extends VkObjectTableEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTableEntryNVX implements Structure.ByReference { }
	
	public VkObjectEntryTypeNVX type;
	public int flags;
}
