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
	"buffer",
	"bindCount",
	"pBinds"
})
public class VkSparseBufferMemoryBindInfo extends VulkanStructure {
	public static class ByValue extends VkSparseBufferMemoryBindInfo implements Structure.ByValue { }
	public static class ByReference extends VkSparseBufferMemoryBindInfo implements Structure.ByReference { }
	
	public Pointer buffer;
	public int bindCount;
	public Pointer pBinds;
}
