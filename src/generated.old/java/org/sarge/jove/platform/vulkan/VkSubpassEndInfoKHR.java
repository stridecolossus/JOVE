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
	"pNext"
})
public class VkSubpassEndInfoKHR extends Structure {
	public static class ByValue extends VkSubpassEndInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassEndInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SUBPASS_END_INFO_KHR.value();
	public Pointer pNext;
}
