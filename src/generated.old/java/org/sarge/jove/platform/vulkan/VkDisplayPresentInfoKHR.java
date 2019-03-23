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
	"srcRect",
	"dstRect",
	"persistent"
})
public class VkDisplayPresentInfoKHR extends Structure {
	public static class ByValue extends VkDisplayPresentInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkDisplayPresentInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DISPLAY_PRESENT_INFO_KHR.value();
	public Pointer pNext;
	public VkRect2D srcRect;
	public VkRect2D dstRect;
	public boolean persistent;
}
