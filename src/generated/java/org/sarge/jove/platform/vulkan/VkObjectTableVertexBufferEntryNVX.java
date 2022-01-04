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
	"buffer"
})
public class VkObjectTableVertexBufferEntryNVX extends VulkanStructure {
	public static class ByValue extends VkObjectTableVertexBufferEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTableVertexBufferEntryNVX implements Structure.ByReference { }
	
	public VkObjectEntryTypeNVX type;
	public int flags;
	public Pointer buffer;
}
