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
	"timeDomain"
})
public class VkCalibratedTimestampInfoEXT extends Structure {
	public static class ByValue extends VkCalibratedTimestampInfoEXT implements Structure.ByValue { }
	public static class ByReference extends VkCalibratedTimestampInfoEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_CALIBRATED_TIMESTAMP_INFO_EXT.value();
	public Pointer pNext;
	public int timeDomain;
}
