package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"tokenType",
	"bindingUnit",
	"dynamicCount",
	"divisor"
})
public class VkIndirectCommandsLayoutTokenNVX extends VulkanStructure {
	public static class ByValue extends VkIndirectCommandsLayoutTokenNVX implements Structure.ByValue { }
	public static class ByReference extends VkIndirectCommandsLayoutTokenNVX implements Structure.ByReference { }
	
	public VkIndirectCommandsTokenTypeNVX tokenType;
	public int bindingUnit;
	public int dynamicCount;
	public int divisor;
}
