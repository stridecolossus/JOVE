package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPhysicalDeviceTransformFeedbackPropertiesEXT implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
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

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			JAVA_INT.withName("maxTransformFeedbackStreams"),
			JAVA_INT.withName("maxTransformFeedbackBuffers"),
			JAVA_LONG.withName("maxTransformFeedbackBufferSize"),
			JAVA_INT.withName("maxTransformFeedbackStreamDataSize"),
			JAVA_INT.withName("maxTransformFeedbackBufferDataSize"),
			JAVA_INT.withName("maxTransformFeedbackBufferDataStride"),
			JAVA_INT.withName("transformFeedbackQueries"),
			JAVA_INT.withName("transformFeedbackStreamsLinesTriangles"),
			JAVA_INT.withName("transformFeedbackRasterizationStreamSelect"),
			JAVA_INT.withName("transformFeedbackDraw")
		);
	}
}
