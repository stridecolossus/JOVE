package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"buffer",
	"bindCount",
	"pBinds"
})
public class VkSparseBufferMemoryBindInfo extends Structure {
	public static class ByValue extends VkSparseBufferMemoryBindInfo implements Structure.ByValue { }
	public static class ByReference extends VkSparseBufferMemoryBindInfo implements Structure.ByReference { }
	
	public long buffer;
	public int bindCount;
	public VkSparseMemoryBind.ByReference pBinds;
}
