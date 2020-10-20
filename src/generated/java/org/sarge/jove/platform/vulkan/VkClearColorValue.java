package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure.FieldOrder;
import com.sun.jna.Union;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"float32",
	"int32",
	"uint32"
})
public class VkClearColorValue extends Union {
	public float[] float32 = new float[4];
	public int[] int32 = new int[4];
	public int[] uint32 = new int[4];
}
