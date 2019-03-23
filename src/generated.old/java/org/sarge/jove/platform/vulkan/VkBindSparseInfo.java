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
	"waitSemaphoreCount",
	"pWaitSemaphores",
	"bufferBindCount",
	"pBufferBinds",
	"imageOpaqueBindCount",
	"pImageOpaqueBinds",
	"imageBindCount",
	"pImageBinds",
	"signalSemaphoreCount",
	"pSignalSemaphores"
})
public class VkBindSparseInfo extends Structure {
	public static class ByValue extends VkBindSparseInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindSparseInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_BIND_SPARSE_INFO.value();
	public Pointer pNext;
	public int waitSemaphoreCount;
	public long pWaitSemaphores;
	public int bufferBindCount;
	public VkSparseBufferMemoryBindInfo.ByReference pBufferBinds;
	public int imageOpaqueBindCount;
	public VkSparseImageOpaqueMemoryBindInfo.ByReference pImageOpaqueBinds;
	public int imageBindCount;
	public VkSparseImageMemoryBindInfo.ByReference pImageBinds;
	public int signalSemaphoreCount;
	public long pSignalSemaphores;
}
