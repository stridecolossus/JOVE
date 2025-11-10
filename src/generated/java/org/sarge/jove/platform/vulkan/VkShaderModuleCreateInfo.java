package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkShaderModuleCreateInfo implements NativeStructure {
	public final VkStructureType sType = VkStructureType.SHADER_MODULE_CREATE_INFO;
	public Handle pNext;
	public int flags;
	public long codeSize;
	public byte[] pCode;

	@Override
	public GroupLayout layout() {
	    return MemoryLayout.structLayout(
	            JAVA_INT.withName("sType"),
	            PADDING,
	            POINTER.withName("pNext"),
	            JAVA_INT.withName("flags"),
	            PADDING,
	            JAVA_LONG.withName("codeSize"),
	            POINTER.withName("pCode")
	    );
	}
}
