package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Union;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"color",
	"depthStencil"
})
public class VkClearValue extends Union {
	public static class ByReference extends VkClearValue implements Union.ByReference { }
	public VkClearColorValue color;
	public VkClearDepthStencilValue depthStencil;
}
