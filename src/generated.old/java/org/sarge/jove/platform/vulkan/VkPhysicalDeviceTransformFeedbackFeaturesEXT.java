package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Pointer;

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
public class VkPhysicalDeviceTransformFeedbackFeaturesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceTransformFeedbackFeaturesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceTransformFeedbackFeaturesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_TRANSFORM_FEEDBACK_FEATURES_EXT.value();
	public Pointer pNext;
	public boolean transformFeedback;
	public boolean geometryStreams;
}
