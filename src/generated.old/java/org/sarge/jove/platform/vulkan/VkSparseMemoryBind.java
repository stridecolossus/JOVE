package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"resourceOffset",
	"size",
	"memory",
	"memoryOffset",
	"flags"
})
public class VkSparseMemoryBind extends Structure {
	public static class ByValue extends VkSparseMemoryBind implements Structure.ByValue { }
	public static class ByReference extends VkSparseMemoryBind implements Structure.ByReference { }
	
	public long resourceOffset;
	public long size;
	public long memory;
	public long memoryOffset;
	public int flags;
}
