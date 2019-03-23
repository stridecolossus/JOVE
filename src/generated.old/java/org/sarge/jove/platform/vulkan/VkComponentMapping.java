package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"r",
	"g",
	"b",
	"a"
})
public class VkComponentMapping extends Structure {
	public static class ByValue extends VkComponentMapping implements Structure.ByValue { }
	public static class ByReference extends VkComponentMapping implements Structure.ByReference { }

	public int r;
	public int g;
	public int b;
	public int a;
}
