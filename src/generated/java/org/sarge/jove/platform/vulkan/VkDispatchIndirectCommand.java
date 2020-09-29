package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"x",
	"y",
	"z"
})
public class VkDispatchIndirectCommand extends VulkanStructure {
	public static class ByValue extends VkDispatchIndirectCommand implements Structure.ByValue { }
	public static class ByReference extends VkDispatchIndirectCommand implements Structure.ByReference { }
	
	public int x;
	public int y;
	public int z;
}
