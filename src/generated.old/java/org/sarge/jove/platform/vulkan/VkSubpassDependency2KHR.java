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
	"srcSubpass",
	"dstSubpass",
	"srcStageMask",
	"dstStageMask",
	"srcAccessMask",
	"dstAccessMask",
	"dependencyFlags",
	"viewOffset"
})
public class VkSubpassDependency2KHR extends Structure {
	public static class ByValue extends VkSubpassDependency2KHR implements Structure.ByValue { }
	public static class ByReference extends VkSubpassDependency2KHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_SUBPASS_DEPENDENCY_2_KHR.value();
	public Pointer pNext;
	public int srcSubpass;
	public int dstSubpass;
	public int srcStageMask;
	public int dstStageMask;
	public int srcAccessMask;
	public int dstAccessMask;
	public int dependencyFlags;
	public int viewOffset;
}
