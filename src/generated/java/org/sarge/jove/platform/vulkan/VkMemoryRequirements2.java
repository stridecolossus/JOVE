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
	"sType",
	"pNext",
	"memoryRequirements"
})
public class VkMemoryRequirements2 extends VulkanStructure {
	public static class ByValue extends VkMemoryRequirements2 implements Structure.ByValue { }
	public static class ByReference extends VkMemoryRequirements2 implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.MEMORY_REQUIREMENTS_2;
	public Pointer pNext;
	public VkMemoryRequirements memoryRequirements;
}
