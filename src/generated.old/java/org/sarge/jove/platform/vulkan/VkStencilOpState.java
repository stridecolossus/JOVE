package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"failOp",
	"passOp",
	"depthFailOp",
	"compareOp",
	"compareMask",
	"writeMask",
	"reference"
})
public class VkStencilOpState extends Structure {
	public static class ByValue extends VkStencilOpState implements Structure.ByValue { }
	public static class ByReference extends VkStencilOpState implements Structure.ByReference { }
	
	public int failOp;
	public int passOp;
	public int depthFailOp;
	public int compareOp;
	public int compareMask;
	public int writeMask;
	public int reference;
}
