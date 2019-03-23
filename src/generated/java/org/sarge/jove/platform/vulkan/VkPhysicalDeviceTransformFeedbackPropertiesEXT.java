package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.VulkanBoolean;
import com.sun.jna.Pointer;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"sType",
	"pNext",
	"maxTransformFeedbackStreams",
	"maxTransformFeedbackBuffers",
	"maxTransformFeedbackBufferSize",
	"maxTransformFeedbackStreamDataSize",
	"maxTransformFeedbackBufferDataSize",
	"maxTransformFeedbackBufferDataStride",
	"transformFeedbackQueries",
	"transformFeedbackStreamsLinesTriangles",
	"transformFeedbackRasterizationStreamSelect",
	"transformFeedbackDraw"
})
public class VkPhysicalDeviceTransformFeedbackPropertiesEXT extends VulkanStructure {
	public static class ByValue extends VkPhysicalDeviceTransformFeedbackPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceTransformFeedbackPropertiesEXT implements Structure.ByReference { }
	
	public VkStructureType sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_TRANSFORM_FEEDBACK_PROPERTIES_EXT;
	public Pointer pNext;
	public int maxTransformFeedbackStreams;
	public int maxTransformFeedbackBuffers;
	public long maxTransformFeedbackBufferSize;
	public int maxTransformFeedbackStreamDataSize;
	public int maxTransformFeedbackBufferDataSize;
	public int maxTransformFeedbackBufferDataStride;
	public VulkanBoolean transformFeedbackQueries;
	public VulkanBoolean transformFeedbackStreamsLinesTriangles;
	public VulkanBoolean transformFeedbackRasterizationStreamSelect;
	public VulkanBoolean transformFeedbackDraw;
}
