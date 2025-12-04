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
public class VkImportSemaphoreFdInfoKHR implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public Handle semaphore;
	public EnumMask<VkSemaphoreImportFlags> flags;
	public EnumMask<VkExternalSemaphoreHandleTypeFlags> handleType;
	public int fd;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			POINTER.withName("semaphore"),
			JAVA_INT.withName("flags"),
			JAVA_INT.withName("handleType"),
			JAVA_INT.withName("fd")
		);
	}
}
