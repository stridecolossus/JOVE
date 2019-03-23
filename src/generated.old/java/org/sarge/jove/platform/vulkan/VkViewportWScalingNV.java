package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"xcoeff",
	"ycoeff"
})
public class VkViewportWScalingNV extends Structure {
	public static class ByValue extends VkViewportWScalingNV implements Structure.ByValue { }
	public static class ByReference extends VkViewportWScalingNV implements Structure.ByReference { }
	
	public float xcoeff;
	public float ycoeff;
}
