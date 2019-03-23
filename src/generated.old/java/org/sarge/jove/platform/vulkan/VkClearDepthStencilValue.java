package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"depth",
	"stencil"
})
public class VkClearDepthStencilValue extends Structure {
	public static class ByValue extends VkClearDepthStencilValue implements Structure.ByValue { }
	public static class ByReference extends VkClearDepthStencilValue implements Structure.ByReference { }
	
	public float depth;
	public int stencil;
}
