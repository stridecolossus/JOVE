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
	"buffer"
})
public class VkObjectTableVertexBufferEntryNVX extends Structure {
	public static class ByValue extends VkObjectTableVertexBufferEntryNVX implements Structure.ByValue { }
	public static class ByReference extends VkObjectTableVertexBufferEntryNVX implements Structure.ByReference { }
	
	public int type;
	public int flags;
	public long buffer;
}
