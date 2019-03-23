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
public class VkSparseImageMemoryBindInfo extends Structure {
	public static class ByValue extends VkSparseImageMemoryBindInfo implements Structure.ByValue { }
	public static class ByReference extends VkSparseImageMemoryBindInfo implements Structure.ByReference { }
	
	public long image;
	public int bindCount;
	public VkSparseImageMemoryBind.ByReference pBinds;
}
