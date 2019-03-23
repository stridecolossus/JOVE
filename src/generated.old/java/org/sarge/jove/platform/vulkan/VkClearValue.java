package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"color",
	"depthStencil"
})
public class VkClearValue extends Structure {
	public static class ByValue extends VkClearValue implements Structure.ByValue { }
	public static class ByReference extends VkClearValue implements Structure.ByReference { }
	
	public VkClearColorValue color;
	public VkClearDepthStencilValue depthStencil;
}
