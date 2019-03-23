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
	"contents"
})
public class VkSubpassBeginInfoKHR extends Structure {
	public static class ByValue extends VkSubpassBeginInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassBeginInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SUBPASS_BEGIN_INFO_KHR.value();
	public Pointer pNext;
	public int contents;
}
