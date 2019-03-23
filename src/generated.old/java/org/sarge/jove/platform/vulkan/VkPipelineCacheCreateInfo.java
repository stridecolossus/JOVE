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
	"initialDataSize",
	"pInitialData"
})
public class VkPipelineCacheCreateInfo extends Structure {
	public static class ByValue extends VkPipelineCacheCreateInfo implements Structure.ByValue { }
	public static class ByReference extends VkPipelineCacheCreateInfo implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PIPELINE_CACHE_CREATE_INFO.value();
	public Pointer pNext;
	public int flags;
	public long initialDataSize;
	public Pointer pInitialData;
}
