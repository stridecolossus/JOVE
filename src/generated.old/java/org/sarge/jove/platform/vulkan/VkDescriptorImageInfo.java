package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sampler",
	"imageView",
	"imageLayout"
})
public class VkDescriptorImageInfo extends Structure {
	public static class ByValue extends VkDescriptorImageInfo implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorImageInfo implements Structure.ByReference { }
	
	public long sampler;
	public long imageView;
	public int imageLayout;
}
