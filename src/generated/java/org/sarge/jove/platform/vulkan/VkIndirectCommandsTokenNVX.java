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
	"tokenType",
	"buffer",
	"offset"
})
public class VkIndirectCommandsTokenNVX extends VulkanStructure {
	public static class ByValue extends VkIndirectCommandsTokenNVX implements Structure.ByValue { }
	public static class ByReference extends VkIndirectCommandsTokenNVX implements Structure.ByReference { }
	
	public VkIndirectCommandsTokenTypeNVX tokenType;
	public Pointer buffer;
	public long offset;
}
