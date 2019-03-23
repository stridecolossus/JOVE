package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"flags",
	"queueFamilyIndex"
})
public class VkCommandPoolCreateInfo extends Structure {
	public static class ByValue extends VkCommandPoolCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkCommandPoolCreateInfo implements Structure.ByReference { }

	public int sType = VkStructureType.VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public int queueFamilyIndex;
}
