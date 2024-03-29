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
	"image",
	"bindCount",
	"pBinds"
})
public class VkSparseImageOpaqueMemoryBindInfo extends VulkanStructure {
	public static class ByValue extends VkSparseImageOpaqueMemoryBindInfo implements Structure.ByValue { }
	public static class ByReference extends VkSparseImageOpaqueMemoryBindInfo implements Structure.ByReference { }
	
	public Pointer image;
	public int bindCount;
	public Pointer pBinds;
}
