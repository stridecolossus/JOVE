package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"srcOffset",
	"dstOffset",
	"size"
})
public class VkBufferCopy extends Structure {
	public static class ByValue extends VkBufferCopy implements Structure.ByValue { }
	public static class ByReference extends VkBufferCopy implements Structure.ByReference { }
	
	public long srcOffset;
	public long dstOffset;
	public long size;
}
