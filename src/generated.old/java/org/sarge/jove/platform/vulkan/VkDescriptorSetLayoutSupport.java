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
	"supported"
})
public class VkDescriptorSetLayoutSupport extends Structure {
	public static class ByValue extends VkDescriptorSetLayoutSupport implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetLayoutSupport implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_SUPPORT.value();
	public Pointer pNext;
	public boolean supported;
}
