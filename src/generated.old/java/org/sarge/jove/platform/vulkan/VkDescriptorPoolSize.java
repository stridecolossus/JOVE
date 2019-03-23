package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"type",
	"descriptorCount"
})
public class VkDescriptorPoolSize extends Structure {
	public static class ByValue extends VkDescriptorPoolSize implements Structure.ByValue { }
	public static class ByReference extends VkDescriptorPoolSize implements Structure.ByReference { }
	
	public int type;
	public int descriptorCount;
}
