package org.sarge.jove.platform.vulkan;

import java.nio.ByteBuffer;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.Structure.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"mapEntryCount",
	"pMapEntries",
	"dataSize",
	"pData"
})
public class VkSpecializationInfo extends VulkanStructure implements ByReference {
	public int mapEntryCount;
	public VkSpecializationMapEntry pMapEntries;
	public long dataSize;
	public ByteBuffer pData;
}
