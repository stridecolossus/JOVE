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
	"semaphore",
	"flags",
	"handleType",
	"fd"
})
public class VkImportSemaphoreFdInfoKHR extends Structure {
	public static class ByValue extends VkImportSemaphoreFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkImportSemaphoreFdInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_IMPORT_SEMAPHORE_FD_INFO_KHR.value();
	public Pointer pNext;
	public long semaphore;
	public int flags;
	public int handleType;
	public int fd;
}
