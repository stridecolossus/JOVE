package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"width",
	"height",
	"depth"
})
public class VkExtent3D extends VulkanStructure {
	public static class ByValue extends VkExtent3D implements Structure.ByValue { }
	public static class ByReference extends VkExtent3D implements Structure.ByReference { }
	
	public int width;
	public int height;
	public int depth;
}
