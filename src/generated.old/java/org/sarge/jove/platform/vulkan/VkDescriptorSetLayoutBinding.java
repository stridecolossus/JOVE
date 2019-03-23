package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
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
public class VkDescriptorSetLayoutBinding extends Structure {
	public static class ByValue extends VkDescriptorSetLayoutBinding implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorSetLayoutBinding implements Structure.ByReference { }
	
	public int binding;
	public int descriptorType;
	public int descriptorCount;
	public int stageFlags;
	public long pImmutableSamplers;
}
