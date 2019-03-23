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
	"size",
	"usage",
	"sharingMode",
	"queueFamilyIndexCount",
	"pQueueFamilyIndices"
})
public class VkBufferCreateInfo extends Structure {
	public static class ByValue extends VkBufferCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkBufferCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public long size;
	public int usage;
	public int sharingMode;
	public int queueFamilyIndexCount;
	public int pQueueFamilyIndices;
}
