package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"presentID",
	"desiredPresentTime",
	"actualPresentTime",
	"earliestPresentTime",
	"presentMargin"
})
public class VkPastPresentationTimingGOOGLE extends Structure {
	public static class ByValue extends VkPastPresentationTimingGOOGLE implements Structure.ByValue { }
	public static class ByReference extends VkPastPresentationTimingGOOGLE implements Structure.ByReference { }
	
	public int presentID;
	public long desiredPresentTime;
	public long actualPresentTime;
	public long earliestPresentTime;
	public long presentMargin;
}
