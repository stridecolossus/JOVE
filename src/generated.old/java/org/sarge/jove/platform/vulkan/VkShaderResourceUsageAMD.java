package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"numUsedVgprs",
	"numUsedSgprs",
	"ldsSizePerLocalWorkGroup",
	"ldsUsageSizeInBytes",
	"scratchMemUsageInBytes"
})
public class VkShaderResourceUsageAMD extends Structure {
	public static class ByValue extends VkShaderResourceUsageAMD implements Structure.ByValue { }
	public static class ByReference extends VkShaderResourceUsageAMD implements Structure.ByReference { }
	
	public int numUsedVgprs;
	public int numUsedSgprs;
	public int ldsSizePerLocalWorkGroup;
	public long ldsUsageSizeInBytes;
	public long scratchMemUsageInBytes;
}
