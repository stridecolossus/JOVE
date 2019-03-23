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
	"buffer",
	"offset",
	"flags"
})
public class VkConditionalRenderingBeginInfoEXT extends Structure {
	public static class ByValue extends VkConditionalRenderingBeginInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkConditionalRenderingBeginInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_CONDITIONAL_RENDERING_BEGIN_INFO_EXT.value();
	public Pointer pNext;
	public long buffer;
	public long offset;
	public int flags;
}
