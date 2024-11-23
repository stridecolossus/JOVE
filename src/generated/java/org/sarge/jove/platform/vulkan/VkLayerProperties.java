package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.platform.vulkan.common.Version;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkLayerProperties extends NativeStructure {
	public String layerName;
	public int specVersion;
	public int implementationVersion;
	public String description;

	@Override
	protected StructLayout layout() {
		return MemoryLayout.structLayout(
		        MemoryLayout.sequenceLayout(256, JAVA_BYTE).withName("layerName"),
		        JAVA_INT.withName("specVersion"),
		        JAVA_INT.withName("implementationVersion"),
		        MemoryLayout.sequenceLayout(256, JAVA_BYTE).withName("description")
		);
	}

	// TODO
	@Override
	public String toString() {
		return String.format("VkLayerProperties[%s spec=%s ver=%s]", layerName, Version.of(specVersion), implementationVersion);
	}
}
