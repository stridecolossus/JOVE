package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"x",
	"y"
})
public class VkSampleLocationEXT extends VulkanStructure {
	public static class ByValue extends VkSampleLocationEXT implements Structure.ByValue { }
	public static class ByReference extends VkSampleLocationEXT implements Structure.ByReference { }
	
	public float x;
	public float y;
}
