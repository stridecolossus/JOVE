package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"offset",
	"size",
	"rowPitch",
	"arrayPitch",
	"depthPitch"
})
public class VkSubresourceLayout extends Structure {
	public static class ByValue extends VkSubresourceLayout implements Structure.ByValue { }
	public static class ByReference extends VkSubresourceLayout implements Structure.ByReference { }
	
	public long offset;
	public long size;
	public long rowPitch;
	public long arrayPitch;
	public long depthPitch;
}
