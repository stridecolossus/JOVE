package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"shadingRatePaletteEntryCount",
	"pShadingRatePaletteEntries"
})
public class VkShadingRatePaletteNV extends Structure {
	public static class ByValue extends VkShadingRatePaletteNV implements Structure.ByValue { }
	public static class ByReference extends VkShadingRatePaletteNV implements Structure.ByReference { }
	
	public int shadingRatePaletteEntryCount;
	public int pShadingRatePaletteEntries;
}
