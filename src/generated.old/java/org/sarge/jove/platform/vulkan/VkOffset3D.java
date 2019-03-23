package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"x",
	"y",
	"z"
})
public class VkOffset3D extends Structure {
	public static class ByValue extends VkOffset3D implements Structure.ByValue { }
	public static class ByReference extends VkOffset3D implements Structure.ByReference { }
	
	public int x;
	public int y;
	public int z;
}
