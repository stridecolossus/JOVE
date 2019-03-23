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
	"stageFlags"
})
public class VkObjectTablePushConstantEntryNVX extends Structure {
	public static class ByValue extends VkObjectTablePushConstantEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTablePushConstantEntryNVX implements Structure.ByReference { }
	
	public int type;
	public int flags;
	public long pipelineLayout;
	public int stageFlags;
}
