package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"memoryTypeCount",
	"memoryTypes",
	"memoryHeapCount",
	"memoryHeaps"
})
public class VkPhysicalDeviceMemoryProperties extends Structure {
	public static class ByValue extends VkPhysicalDeviceMemoryProperties implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceMemoryProperties implements Structure.ByReference { }
	
	public int memoryTypeCount;
	public final VkMemoryType[] memoryTypes = new VkMemoryType[32];
	public int memoryHeapCount;
	public final VkMemoryHeap[] memoryHeaps = new VkMemoryHeap[16];
}
