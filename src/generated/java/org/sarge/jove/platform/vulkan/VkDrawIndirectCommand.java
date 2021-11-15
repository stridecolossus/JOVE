package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"vertexCount",
	"instanceCount",
	"firstVertex",
	"firstInstance"
})
public class VkDrawIndirectCommand extends VulkanStructure {
	public static class ByValue extends VkDrawIndirectCommand implements Structure.ByValue { }
	public static class ByReference extends VkDrawIndirectCommand implements Structure.ByReference { }
	
	public int vertexCount;
	public int instanceCount;
	public int firstVertex;
	public int firstInstance;
}
