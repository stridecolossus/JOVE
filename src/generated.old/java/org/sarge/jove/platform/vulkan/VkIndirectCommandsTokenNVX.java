package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"tokenType",
	"buffer",
	"offset"
})
public class VkIndirectCommandsTokenNVX extends Structure {
	public static class ByValue extends VkIndirectCommandsTokenNVX implements Structure.ByValue { }
	public static class ByReference extends VkIndirectCommandsTokenNVX implements Structure.ByReference { }
	
	public int tokenType;
	public long buffer;
	public long offset;
}
