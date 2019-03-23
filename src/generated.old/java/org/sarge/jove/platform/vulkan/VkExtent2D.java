package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"width",
	"height"
})
public class VkExtent2D extends Structure {
	public static class ByValue extends VkExtent2D implements Structure.ByValue { }
	public static class ByReference extends VkExtent2D implements Structure.ByReference { }
	
	public int width;
	public int height;
}
