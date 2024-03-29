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
	"type",
	"flags",
	"buffer",
	"indexType"
})
public class VkObjectTableIndexBufferEntryNVX extends VulkanStructure {
	public static class ByValue extends VkObjectTableIndexBufferEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTableIndexBufferEntryNVX implements Structure.ByReference { }
	
	public VkObjectEntryTypeNVX type;
	public int flags;
	public Pointer buffer;
	public VkIndexType indexType;
}
