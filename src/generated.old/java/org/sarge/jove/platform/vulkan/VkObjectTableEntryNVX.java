package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"type",
	"flags"
})
public class VkObjectTableEntryNVX extends Structure {
	public static class ByValue extends VkObjectTableEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTableEntryNVX implements Structure.ByReference { }
	
	public int type;
	public int flags;
}
