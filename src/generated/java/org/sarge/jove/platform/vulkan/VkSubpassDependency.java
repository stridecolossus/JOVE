package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.util.EnumMask;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkSubpassDependency implements NativeStructure {
	public int srcSubpass;
	public int dstSubpass;
	public EnumMask<VkPipelineStage> srcStageMask;
	public EnumMask<VkPipelineStage> dstStageMask;
	public EnumMask<VkAccess> srcAccessMask;
	public EnumMask<VkAccess> dstAccessMask;
	public EnumMask<VkDependencyFlag> dependencyFlags;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("srcSubpass"),
				JAVA_INT.withName("dstSubpass"),
				JAVA_INT.withName("srcStageMask"),
				JAVA_INT.withName("dstStageMask"),
				JAVA_INT.withName("srcAccessMask"),
				JAVA_INT.withName("dstAccessMask"),
				JAVA_INT.withName("dependencyFlags")
		);
	}
}
