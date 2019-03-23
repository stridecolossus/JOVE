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
	"fence",
	"flags",
	"handleType",
	"fd"
})
public class VkImportFenceFdInfoKHR extends Structure {
	public static class ByValue extends VkImportFenceFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkImportFenceFdInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMPORT_FENCE_FD_INFO_KHR.value();
	public Pointer pNext;
	public long fence;
	public int flags;
	public int handleType;
	public int fd;
}
