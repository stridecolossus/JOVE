package org.sarge.jove.platform.vulkan;

import com.sun.jna.*;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"color",
	"depthStencil"
})
public class VkClearValue extends Union {
	public static class ByReference extends VkClearValue implements Structure.ByReference { }
	public VkClearColorValue color;
	public VkClearDepthStencilValue depthStencil;
}
