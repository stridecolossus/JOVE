package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"transformFeedback",
	"geometryStreams"
})
public class VkPhysicalDeviceTransformFeedbackFeaturesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceTransformFeedbackFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceTransformFeedbackFeaturesEXT implements Structure.ByReference { }

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_TRANSFORM_FEEDBACK_FEATURES_EXT;
	public Pointer pNext;
	public boolean transformFeedback;
	public boolean geometryStreams;
}
