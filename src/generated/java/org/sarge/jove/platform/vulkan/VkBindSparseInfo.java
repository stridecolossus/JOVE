package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

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
public class VkBindSparseInfo extends VulkanStructure {
	public static class ByValue extends VkBindSparseInfo implements Structure.ByValue { }
	public static class ByReference extends VkBindSparseInfo implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.BIND_SPARSE_INFO;
	public Pointer pNext;
	public int waitSemaphoreCount;
	public Pointer pWaitSemaphores;
	public int bufferBindCount;
	public Pointer pBufferBinds;
	public int imageOpaqueBindCount;
	public Pointer pImageOpaqueBinds;
	public int imageBindCount;
	public Pointer pImageBinds;
	public int signalSemaphoreCount;
	public Pointer pSignalSemaphores;
}
