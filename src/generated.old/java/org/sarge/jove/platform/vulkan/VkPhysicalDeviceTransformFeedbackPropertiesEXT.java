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
public class VkPhysicalDeviceTransformFeedbackPropertiesEXT extends Structure {
	public static class ByValue extends VkPhysicalDeviceTransformFeedbackPropertiesEXT implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceTransformFeedbackPropertiesEXT implements Structure.ByReference { }
	
	public final int sType = VkStructureType.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_TRANSFORM_FEEDBACK_PROPERTIES_EXT.value();
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
