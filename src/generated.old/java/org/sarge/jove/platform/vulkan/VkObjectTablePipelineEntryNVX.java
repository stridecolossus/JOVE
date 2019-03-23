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
	"pipeline"
})
public class VkObjectTablePipelineEntryNVX extends Structure {
	public static class ByValue extends VkObjectTablePipelineEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTablePipelineEntryNVX implements Structure.ByReference { }
	
	public int type;
	public int flags;
	public long pipeline;
}
