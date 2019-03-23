package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"rect",
	"baseArrayLayer",
	"layerCount"
})
public class VkClearRect extends Structure {
	public static class ByValue extends VkClearRect implements Structure.ByValue { }
	public static class ByReference extends VkClearRect implements Structure.ByReference { }
	
	public VkRect2D rect;
	public int baseArrayLayer;
	public int layerCount;
}
