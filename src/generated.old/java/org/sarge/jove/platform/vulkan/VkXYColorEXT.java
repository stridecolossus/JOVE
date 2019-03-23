package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"x",
	"y"
})
public class VkXYColorEXT extends Structure {
	public static class ByValue extends VkXYColorEXT implements Structure.ByValue { }
	public static class ByReference extends VkXYColorEXT implements Structure.ByReference { }
	
	public float x;
	public float y;
}
