package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"presentID",
	"desiredPresentTime"
})
public class VkPresentTimeGOOGLE extends VulkanStructure {
	public static class ByValue extends VkPresentTimeGOOGLE implements Structure.ByValue { }
	public static class ByReference extends VkPresentTimeGOOGLE implements Structure.ByReference { }
	
	public int presentID;
	public long desiredPresentTime;
}
