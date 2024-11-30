package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkDebugUtilsMessengerCallbackData implements NativeStructure {
	public /*final*/ VkStructureType sType = VkStructureType.DEBUG_UTILS_MESSENGER_CALLBACK_DATA_EXT;
	public Handle pNext;
	public int flags;
	public String pMessageIdName;
	public int messageIdNumber;
	public String pMessage;
	public int queueLabelCount;
	public Handle pQueueLabels;
	public int cmdBufLabelCount;
	public Handle pCmdBufLabels;
	public int objectCount;
	public Handle pObjects;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
                JAVA_INT.withName("sType"),
                PADDING,
                POINTER.withName("pNext"),
                JAVA_INT.withName("flags"),
                PADDING,
                POINTER.withName("pMessageIdName"),
                JAVA_INT.withName("messageIdNumber"),
                PADDING,
                POINTER.withName("pMessage"),
                JAVA_INT.withName("queueLabelCount"),
                PADDING,
                POINTER.withName("pQueueLabels"),
                JAVA_INT.withName("cmdBufLabelCount"),
                PADDING,
                POINTER.withName("pCmdBufLabels"),
                JAVA_INT.withName("objectCount"),
                PADDING,
                POINTER.withName("pObjects")
		);
	}
}
