package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitField;

import com.sun.jna.Structure.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"stageFlags",
	"offset",
	"size"
})
public class VkPushConstantRange extends VulkanStructure implements ByReference {
	public BitField<VkShaderStage> stageFlags;
	public int offset;
	public int size;
}
