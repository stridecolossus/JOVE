package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure.*;
import com.sun.jna.Union;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"color",
	"depthStencil"
})
public class VkClearValue extends Union implements ByReference {
	public VkClearColorValue color;
	public VkClearDepthStencilValue depthStencil;
}
