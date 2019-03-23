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
	"width",
	"height",
	"minDepth",
	"maxDepth"
})
public class VkViewport extends Structure {
	public static class ByValue extends VkViewport implements Structure.ByValue { }
	public static class ByReference extends VkViewport implements Structure.ByReference { }
	
	public float x;
	public float y;
	public float width;
	public float height;
	public float minDepth;
	public float maxDepth;
}
