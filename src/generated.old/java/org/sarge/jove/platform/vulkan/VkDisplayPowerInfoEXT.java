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
	"powerState"
})
public class VkDisplayPowerInfoEXT extends Structure {
	public static class ByValue extends VkDisplayPowerInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkDisplayPowerInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_DISPLAY_POWER_INFO_EXT.value();
	public Pointer pNext;
	public int powerState;
}
