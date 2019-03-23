package org.sarge.jove.platform.vulkan;

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
	"flags",
	"topology",
	"primitiveRestartEnable"
})
public class VkPipelineInputAssemblyStateCreateInfo extends Structure {
	public static class ByValue extends VkPipelineInputAssemblyStateCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineInputAssemblyStateCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST.value();
	public boolean primitiveRestartEnable;

	public static class Builder {
		// TODO
	}
}
