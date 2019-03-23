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
	"handleType"
})
public class VkFenceGetFdInfoKHR extends Structure {
	public static class ByValue extends VkFenceGetFdInfoKHR implements Structure.ByValue { }
	public static class ByReference extends VkFenceGetFdInfoKHR implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_FENCE_GET_FD_INFO_KHR.value();
	public Pointer pNext;
	public long fence;
	public int handleType;
}
