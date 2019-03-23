package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"mapEntryCount",
	"pMapEntries",
	"dataSize",
	"pData"
})
public class VkSpecializationInfo extends Structure {
	public static class ByValue extends VkSpecializationInfo implements Structure.ByValue { }
	public static class ByReference extends VkSpecializationInfo implements Structure.ByReference { }
	
	public int mapEntryCount;
	public VkSpecializationMapEntry.ByReference pMapEntries;
	public long dataSize;
	public Pointer pData;
}
