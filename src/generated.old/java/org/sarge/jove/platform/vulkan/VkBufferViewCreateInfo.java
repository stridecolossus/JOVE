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
	"buffer",
	"format",
	"offset",
	"range"
})
public class VkBufferViewCreateInfo extends Structure {
	public static class ByValue extends VkBufferViewCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkBufferViewCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BUFFER_VIEW_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public long buffer;
	public int format;
	public long offset;
	public long range;
}
