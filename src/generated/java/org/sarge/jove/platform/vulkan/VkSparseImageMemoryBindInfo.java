package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"image",
	"bindCount",
	"pBinds"
})
public class VkSparseImageMemoryBindInfo extends VulkanStructure {
	public static class ByValue extends VkSparseImageMemoryBindInfo implements Structure.ByValue { }
	public static class ByReference extends VkSparseImageMemoryBindInfo implements Structure.ByReference { }
	
	public Pointer image;
	public int bindCount;
	public Pointer pBinds;
}
