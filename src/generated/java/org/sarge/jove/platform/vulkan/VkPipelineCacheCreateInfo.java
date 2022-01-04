package org.sarge.jove.platform.vulkan;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.FieldOrder;

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
public class VkPipelineCacheCreateInfo extends VulkanStructure {
	public VkStructureType sType = VkStructureType.PIPELINE_CACHE_CREATE_INFO;
	public Pointer pNext;
	public int flags;
	public long initialDataSize;
	public ByteBuffer pInitialData;
}
