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
public class VkExtensionProperties implements NativeStructure {
	public String extensionName;
	public int specVersion;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			MemoryLayout.sequenceLayout(256, JAVA_CHAR).withName("extensionName"),
			JAVA_INT.withName("specVersion")
		);
	}
}
