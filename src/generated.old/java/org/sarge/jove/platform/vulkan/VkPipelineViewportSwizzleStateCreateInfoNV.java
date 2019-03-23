package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"viewportCount",
	"pViewportSwizzles"
})
public class VkPipelineViewportSwizzleStateCreateInfoNV extends Structure {
	public static class ByValue extends VkPipelineViewportSwizzleStateCreateInfoNV implements Structure.ByValue { }
	public static class ByReference extends VkPipelineViewportSwizzleStateCreateInfoNV implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_SWIZZLE_STATE_CREATE_INFO_NV.value();
	public Pointer pNext;
	public int flags;
	public int viewportCount;
	public VkViewportSwizzleNV.ByReference pViewportSwizzles;
}
