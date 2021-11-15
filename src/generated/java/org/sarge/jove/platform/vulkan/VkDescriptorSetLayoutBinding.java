package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Pointer;
import com.sun.jna.Structure.ByReference;
import com.sun.jna.Structure.FieldOrder;

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
	public int stageFlags;
	public Pointer pImmutableSamplers;
}
