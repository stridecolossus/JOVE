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
	"type",
	"flags",
	"pipeline"
})
public class VkObjectTablePipelineEntryNVX extends VulkanStructure {
	public static class ByValue extends VkObjectTablePipelineEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTablePipelineEntryNVX implements Structure.ByReference { }
	
	public VkObjectEntryTypeNVX type;
	public int flags;
	public Pointer pipeline;
}
