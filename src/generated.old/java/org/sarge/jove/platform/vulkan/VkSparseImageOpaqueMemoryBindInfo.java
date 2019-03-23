package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"image",
	"bindCount",
	"pBinds"
})
public class VkSparseImageOpaqueMemoryBindInfo extends Structure {
	public static class ByValue extends VkSparseImageOpaqueMemoryBindInfo implements Structure.ByValue { }
	public static class ByReference extends VkSparseImageOpaqueMemoryBindInfo implements Structure.ByReference { }
	
	public long image;
	public int bindCount;
	public VkSparseMemoryBind.ByReference pBinds;
}
