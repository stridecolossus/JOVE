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
	"z",
	"w"
})
public class VkViewportSwizzleNV extends Structure {
	public static class ByValue extends VkViewportSwizzleNV implements Structure.ByValue { }
	public static class ByReference extends VkViewportSwizzleNV implements Structure.ByReference { }
	
	public int x;
	public int y;
	public int z;
	public int w;
}
