package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"size",
	"alignment",
	"memoryTypeBits"
})
public class VkMemoryRequirements extends Structure {
	public static class ByValue extends VkMemoryRequirements implements Structure.ByValue { }
	public static class ByReference extends VkMemoryRequirements implements Structure.ByReference { }
	
	public long size;
	public long alignment;
	public int memoryTypeBits;
}
