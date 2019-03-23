package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"constantID",
	"offset",
	"size"
})
public class VkSpecializationMapEntry extends Structure {
	public static class ByValue extends VkSpecializationMapEntry implements Structure.ByValue { }
	public static class ByReference extends VkSpecializationMapEntry implements Structure.ByReference { }
	
	public int constantID;
	public int offset;
	public long size;
}
