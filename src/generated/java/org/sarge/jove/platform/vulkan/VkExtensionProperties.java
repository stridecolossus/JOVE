package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_BYTE;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkExtensionProperties implements NativeStructure {
	public String extensionName; // byte[] extensionName = new byte[256];
	public int specVersion;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
		        MemoryLayout.sequenceLayout(256, JAVA_BYTE).withName("extensionName"),
				ValueLayout.JAVA_INT.withName("specVersion")
		);
	}
}
