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

	public VkStructureType sType = VkStructureType.PHYSICAL_DEVICE_TRANSFORM_FEEDBACK_PROPERTIES_EXT;
	public Pointer pNext;
	public int maxTransformFeedbackStreams;
	public int maxTransformFeedbackBuffers;
	public long maxTransformFeedbackBufferSize;
	public int maxTransformFeedbackStreamDataSize;
	public int maxTransformFeedbackBufferDataSize;
	public int maxTransformFeedbackBufferDataStride;
	public boolean transformFeedbackQueries;
	public boolean transformFeedbackStreamsLinesTriangles;
	public boolean transformFeedbackRasterizationStreamSelect;
	public boolean transformFeedbackDraw;
}
