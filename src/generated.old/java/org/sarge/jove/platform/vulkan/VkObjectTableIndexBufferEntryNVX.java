package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"type",
	"flags",
	"buffer",
	"indexType"
})
public class VkObjectTableIndexBufferEntryNVX extends Structure {
	public static class ByValue extends VkObjectTableIndexBufferEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTableIndexBufferEntryNVX implements Structure.ByReference { }
	
	public int type;
	public int flags;
	public long buffer;
	public int indexType;
}
