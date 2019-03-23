package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"maxSets",
	"poolSizeCount",
	"pPoolSizes"
})
public class VkDescriptorPoolCreateInfo extends Structure {
	public static class ByValue extends VkDescriptorPoolCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorPoolCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int maxSets;
	public int poolSizeCount;
	public VkDescriptorPoolSize.ByReference pPoolSizes;
}
