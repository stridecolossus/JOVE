package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"location",
	"binding",
	"format",
	"offset"
})
public class VkVertexInputAttributeDescription extends Structure {
	public static class ByValue extends VkVertexInputAttributeDescription implements Structure.ByValue { }
	public static class ByReference extends VkVertexInputAttributeDescription implements Structure.ByReference { }
	
	public int location;
	public int binding;
	public int format;
	public int offset;
}
