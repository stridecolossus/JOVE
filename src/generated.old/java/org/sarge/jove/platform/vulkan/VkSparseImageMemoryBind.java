package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"subresource",
	"offset",
	"extent",
	"memory",
	"memoryOffset",
	"flags"
})
public class VkSparseImageMemoryBind extends Structure {
	public static class ByValue extends VkSparseImageMemoryBind implements Structure.ByValue { }
	public static class ByReference extends VkSparseImageMemoryBind implements Structure.ByReference { }
	
	public VkImageSubresource subresource;
	public VkOffset3D offset;
	public VkExtent3D extent;
	public long memory;
	public long memoryOffset;
	public int flags;
}
