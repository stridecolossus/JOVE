package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"indexCount",
	"instanceCount",
	"firstIndex",
	"vertexOffset",
	"firstInstance"
})
public class VkDrawIndexedIndirectCommand extends VulkanStructure {
	public static class ByValue extends VkDrawIndexedIndirectCommand implements Structure.ByValue { }
	public static class ByReference extends VkDrawIndexedIndirectCommand implements Structure.ByReference { }
	
	public int indexCount;
	public int instanceCount;
	public int firstIndex;
	public int vertexOffset;
	public int firstInstance;
}
