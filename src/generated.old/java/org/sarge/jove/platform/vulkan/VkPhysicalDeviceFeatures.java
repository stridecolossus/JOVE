package org.sarge.jove.platform.vulkan;

import com.sun.jna.Structure;
import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"robustBufferAccess",
	"fullDrawIndexUint32",
	"imageCubeArray",
	"independentBlend",
	"geometryShader",
	"tessellationShader",
	"sampleRateShading",
	"dualSrcBlend",
	"logicOp",
	"multiDrawIndirect",
	"drawIndirectFirstInstance",
	"depthClamp",
	"depthBiasClamp",
	"fillModeNonSolid",
	"depthBounds",
	"wideLines",
	"largePoints",
	"alphaToOne",
	"multiViewport",
	"samplerAnisotropy",
	"textureCompressionETC2",
	"textureCompressionASTC_LDR",
	"textureCompressionBC",
	"occlusionQueryPrecise",
	"pipelineStatisticsQuery",
	"vertexPipelineStoresAndAtomics",
	"fragmentStoresAndAtomics",
	"shaderTessellationAndGeometryPointSize",
	"shaderImageGatherExtended",
	"shaderStorageImageExtendedFormats",
	"shaderStorageImageMultisample",
	"shaderStorageImageReadWithoutFormat",
	"shaderStorageImageWriteWithoutFormat",
	"shaderUniformBufferArrayDynamicIndexing",
	"shaderSampledImageArrayDynamicIndexing",
	"shaderStorageBufferArrayDynamicIndexing",
	"shaderStorageImageArrayDynamicIndexing",
	"shaderClipDistance",
	"shaderCullDistance",
	"shaderFloat64",
	"shaderInt64",
	"shaderInt16",
	"shaderResourceResidency",
	"shaderResourceMinLod",
	"sparseBinding",
	"sparseResidencyBuffer",
	"sparseResidencyImage2D",
	"sparseResidencyImage3D",
	"sparseResidency2Samples",
	"sparseResidency4Samples",
	"sparseResidency8Samples",
	"sparseResidency16Samples",
	"sparseResidencyAliased",
	"variableMultisampleRate",
	"inheritedQueries"
})
public class VkPhysicalDeviceFeatures extends Structure {
	public static class ByValue extends VkPhysicalDeviceFeatures implements Structure.ByValue { }
	public static class ByReference extends VkPhysicalDeviceFeatures implements Structure.ByReference { }

	public boolean robustBufferAccess;
	public boolean fullDrawIndexUint32;
	public boolean imageCubeArray;
	public boolean independentBlend;
	public boolean geometryShader;
	public boolean tessellationShader;
	public boolean sampleRateShading;
	public boolean dualSrcBlend;
	public boolean logicOp;
	public boolean multiDrawIndirect;
	public boolean drawIndirectFirstInstance;
	public boolean depthClamp;
	public boolean depthBiasClamp;
	public boolean fillModeNonSolid;
	public boolean depthBounds;
	public boolean wideLines;
	public boolean largePoints;
	public boolean alphaToOne;
	public boolean multiViewport;
	public boolean samplerAnisotropy;
	public boolean textureCompressionETC2;
	public boolean textureCompressionASTC_LDR;
	public boolean textureCompressionBC;
	public boolean occlusionQueryPrecise;
	public boolean pipelineStatisticsQuery;
	public boolean vertexPipelineStoresAndAtomics;
	public boolean fragmentStoresAndAtomics;
	public boolean shaderTessellationAndGeometryPointSize;
	public boolean shaderImageGatherExtended;
	public boolean shaderStorageImageExtendedFormats;
	public boolean shaderStorageImageMultisample;
	public boolean shaderStorageImageReadWithoutFormat;
	public boolean shaderStorageImageWriteWithoutFormat;
	public boolean shaderUniformBufferArrayDynamicIndexing;
	public boolean shaderSampledImageArrayDynamicIndexing;
	public boolean shaderStorageBufferArrayDynamicIndexing;
	public boolean shaderStorageImageArrayDynamicIndexing;
	public boolean shaderClipDistance;
	public boolean shaderCullDistance;
	public boolean shaderFloat64;
	public boolean shaderInt64;
	public boolean shaderInt16;
	public boolean shaderResourceResidency;
	public boolean shaderResourceMinLod;
	public boolean sparseBinding;
	public boolean sparseResidencyBuffer;
	public boolean sparseResidencyImage2D;
	public boolean sparseResidencyImage3D;
	public boolean sparseResidency2Samples;
	public boolean sparseResidency4Samples;
	public boolean sparseResidency8Samples;
	public boolean sparseResidency16Samples;
	public boolean sparseResidencyAliased;
	public boolean variableMultisampleRate;
	public boolean inheritedQueries;
}
