package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"srcSubpass",
	"dstSubpass",
	"srcStageMask",
	"dstStageMask",
	"srcAccessMask",
	"dstAccessMask",
	"dependencyFlags"
})
public class VkSubpassDependency extends Structure {
	public static class ByValue extends VkSubpassDependency implements Structure.ByValue { }
	public static class ByReference extends VkSubpassDependency implements Structure.ByReference { }
	
	public int srcSubpass;
	public int dstSubpass;
	public int srcStageMask;
	public int dstStageMask;
	public int srcAccessMask;
	public int dstAccessMask;
	public int dependencyFlags;
}
