package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"type",
	"flags",
	"pipelineLayout",
	"descriptorSet"
})
public class VkObjectTableDescriptorSetEntryNVX extends Structure {
	public static class ByValue extends VkObjectTableDescriptorSetEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTableDescriptorSetEntryNVX implements Structure.ByReference { }
	
	public int type;
	public int flags;
	public long pipelineLayout;
	public long descriptorSet;
}
