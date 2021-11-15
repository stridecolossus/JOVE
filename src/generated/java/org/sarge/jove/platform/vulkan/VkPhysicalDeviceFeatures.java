package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Structure.ByReference;
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
public class VkPhysicalDeviceFeatures extends VulkanStructure implements ByReference {
	public VulkanBoolean robustBufferAccess;
	public VulkanBoolean fullDrawIndexUint32;
	public VulkanBoolean imageCubeArray;
	public VulkanBoolean independentBlend;
	public VulkanBoolean geometryShader;
	public VulkanBoolean tessellationShader;
	public VulkanBoolean sampleRateShading;
	public VulkanBoolean dualSrcBlend;
	public VulkanBoolean logicOp;
	public VulkanBoolean multiDrawIndirect;
	public VulkanBoolean drawIndirectFirstInstance;
	public VulkanBoolean depthClamp;
	public VulkanBoolean depthBiasClamp;
	public VulkanBoolean fillModeNonSolid;
	public VulkanBoolean depthBounds;
	public VulkanBoolean wideLines;
	public VulkanBoolean largePoints;
	public VulkanBoolean alphaToOne;
	public VulkanBoolean multiViewport;
	public VulkanBoolean samplerAnisotropy;
	public VulkanBoolean textureCompressionETC2;
	public VulkanBoolean textureCompressionASTC_LDR;
	public VulkanBoolean textureCompressionBC;
	public VulkanBoolean occlusionQueryPrecise;
	public VulkanBoolean pipelineStatisticsQuery;
	public VulkanBoolean vertexPipelineStoresAndAtomics;
	public VulkanBoolean fragmentStoresAndAtomics;
	public VulkanBoolean shaderTessellationAndGeometryPointSize;
	public VulkanBoolean shaderImageGatherExtended;
	public VulkanBoolean shaderStorageImageExtendedFormats;
	public VulkanBoolean shaderStorageImageMultisample;
	public VulkanBoolean shaderStorageImageReadWithoutFormat;
	public VulkanBoolean shaderStorageImageWriteWithoutFormat;
	public VulkanBoolean shaderUniformBufferArrayDynamicIndexing;
	public VulkanBoolean shaderSampledImageArrayDynamicIndexing;
	public VulkanBoolean shaderStorageBufferArrayDynamicIndexing;
	public VulkanBoolean shaderStorageImageArrayDynamicIndexing;
	public VulkanBoolean shaderClipDistance;
	public VulkanBoolean shaderCullDistance;
	public VulkanBoolean shaderFloat64;
	public VulkanBoolean shaderInt64;
	public VulkanBoolean shaderInt16;
	public VulkanBoolean shaderResourceResidency;
	public VulkanBoolean shaderResourceMinLod;
	public VulkanBoolean sparseBinding;
	public VulkanBoolean sparseResidencyBuffer;
	public VulkanBoolean sparseResidencyImage2D;
	public VulkanBoolean sparseResidencyImage3D;
	public VulkanBoolean sparseResidency2Samples;
	public VulkanBoolean sparseResidency4Samples;
	public VulkanBoolean sparseResidency8Samples;
	public VulkanBoolean sparseResidency16Samples;
	public VulkanBoolean sparseResidencyAliased;
	public VulkanBoolean variableMultisampleRate;
	public VulkanBoolean inheritedQueries;
}
