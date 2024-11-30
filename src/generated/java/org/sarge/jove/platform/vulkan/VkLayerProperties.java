package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkLayerProperties implements NativeStructure {
	public String layerName;
	public int specVersion;
	public int implementationVersion;
	public String description;

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
		        MemoryLayout.sequenceLayout(256, JAVA_BYTE).withName("layerName"),
		        JAVA_INT.withName("specVersion"),
		        JAVA_INT.withName("implementationVersion"),
		        MemoryLayout.sequenceLayout(256, JAVA_BYTE).withName("description")
		);
	}
}
