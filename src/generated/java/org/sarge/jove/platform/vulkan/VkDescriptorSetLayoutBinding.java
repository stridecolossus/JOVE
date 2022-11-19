package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"binding",
	"descriptorType",
	"descriptorCount",
	"stageFlags",
	"pImmutableSamplers"
})
public class VkDescriptorSetLayoutBinding extends VulkanStructure implements ByReference {
	public int binding;
	public VkDescriptorType descriptorType;
	public int descriptorCount;
	public BitMask<VkShaderStage> stageFlags;
	public Pointer pImmutableSamplers;
}
