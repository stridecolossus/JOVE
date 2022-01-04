package org.sarge.jove.platform.vulkan;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Structure.FieldOrder;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
@FieldOrder({
	"maxImageDimension1D",
	"maxImageDimension2D",
	"maxImageDimension3D",
	"maxImageDimensionCube",
	"maxImageArrayLayers",
	"maxTexelBufferElements",
	"maxUniformBufferRange",
	"maxStorageBufferRange",
	"maxPushConstantsSize",
	"maxMemoryAllocationCount",
	"maxSamplerAllocationCount",
	"bufferImageGranularity",
	"sparseAddressSpaceSize",
	"maxBoundDescriptorSets",
	"maxPerStageDescriptorSamplers",
	"maxPerStageDescriptorUniformBuffers",
	"maxPerStageDescriptorStorageBuffers",
	"maxPerStageDescriptorSampledImages",
	"maxPerStageDescriptorStorageImages",
	"maxPerStageDescriptorInputAttachments",
	"maxPerStageResources",
	"maxDescriptorSetSamplers",
	"maxDescriptorSetUniformBuffers",
	"maxDescriptorSetUniformBuffersDynamic",
	"maxDescriptorSetStorageBuffers",
	"maxDescriptorSetStorageBuffersDynamic",
	"maxDescriptorSetSampledImages",
	"maxDescriptorSetStorageImages",
	"maxDescriptorSetInputAttachments",
	"maxVertexInputAttributes",
	"maxVertexInputBindings",
	"maxVertexInputAttributeOffset",
	"maxVertexInputBindingStride",
	"maxVertexOutputComponents",
	"maxTessellationGenerationLevel",
	"maxTessellationPatchSize",
	"maxTessellationControlPerVertexInputComponents",
	"maxTessellationControlPerVertexOutputComponents",
	"maxTessellationControlPerPatchOutputComponents",
	"maxTessellationControlTotalOutputComponents",
	"maxTessellationEvaluationInputComponents",
	"maxTessellationEvaluationOutputComponents",
	"maxGeometryShaderInvocations",
	"maxGeometryInputComponents",
	"maxGeometryOutputComponents",
	"maxGeometryOutputVertices",
	"maxGeometryTotalOutputComponents",
	"maxFragmentInputComponents",
	"maxFragmentOutputAttachments",
	"maxFragmentDualSrcAttachments",
	"maxFragmentCombinedOutputResources",
	"maxComputeSharedMemorySize",
	"maxComputeWorkGroupCount",
	"maxComputeWorkGroupInvocations",
	"maxComputeWorkGroupSize",
	"subPixelPrecisionBits",
	"subTexelPrecisionBits",
	"mipmapPrecisionBits",
	"maxDrawIndexedIndexValue",
	"maxDrawIndirectCount",
	"maxSamplerLodBias",
	"maxSamplerAnisotropy",
	"maxViewports",
	"maxViewportDimensions",
	"viewportBoundsRange",
	"viewportSubPixelBits",
	"minMemoryMapAlignment",
	"minTexelBufferOffsetAlignment",
	"minUniformBufferOffsetAlignment",
	"minStorageBufferOffsetAlignment",
	"minTexelOffset",
	"maxTexelOffset",
	"minTexelGatherOffset",
	"maxTexelGatherOffset",
	"minInterpolationOffset",
	"maxInterpolationOffset",
	"subPixelInterpolationOffsetBits",
	"maxFramebufferWidth",
	"maxFramebufferHeight",
	"maxFramebufferLayers",
	"framebufferColorSampleCounts",
	"framebufferDepthSampleCounts",
	"framebufferStencilSampleCounts",
	"framebufferNoAttachmentsSampleCounts",
	"maxColorAttachments",
	"sampledImageColorSampleCounts",
	"sampledImageIntegerSampleCounts",
	"sampledImageDepthSampleCounts",
	"sampledImageStencilSampleCounts",
	"storageImageSampleCounts",
	"maxSampleMaskWords",
	"timestampComputeAndGraphics",
	"timestampPeriod",
	"maxClipDistances",
	"maxCullDistances",
	"maxCombinedClipAndCullDistances",
	"discreteQueuePriorities",
	"pointSizeRange",
	"lineWidthRange",
	"pointSizeGranularity",
	"lineWidthGranularity",
	"strictLines",
	"standardSampleLocations",
	"optimalBufferCopyOffsetAlignment",
	"optimalBufferCopyRowPitchAlignment",
	"nonCoherentAtomSize"
})
public class VkPhysicalDeviceLimits extends VulkanStructure {
	public int maxImageDimension1D;
	public int maxImageDimension2D;
	public int maxImageDimension3D;
	public int maxImageDimensionCube;
	public int maxImageArrayLayers;
	public int maxTexelBufferElements;
	public int maxUniformBufferRange;
	public int maxStorageBufferRange;
	public int maxPushConstantsSize;
	public int maxMemoryAllocationCount;
	public int maxSamplerAllocationCount;
	public long bufferImageGranularity;
	public long sparseAddressSpaceSize;
	public int maxBoundDescriptorSets;
	public int maxPerStageDescriptorSamplers;
	public int maxPerStageDescriptorUniformBuffers;
	public int maxPerStageDescriptorStorageBuffers;
	public int maxPerStageDescriptorSampledImages;
	public int maxPerStageDescriptorStorageImages;
	public int maxPerStageDescriptorInputAttachments;
	public int maxPerStageResources;
	public int maxDescriptorSetSamplers;
	public int maxDescriptorSetUniformBuffers;
	public int maxDescriptorSetUniformBuffersDynamic;
	public int maxDescriptorSetStorageBuffers;
	public int maxDescriptorSetStorageBuffersDynamic;
	public int maxDescriptorSetSampledImages;
	public int maxDescriptorSetStorageImages;
	public int maxDescriptorSetInputAttachments;
	public int maxVertexInputAttributes;
	public int maxVertexInputBindings;
	public int maxVertexInputAttributeOffset;
	public int maxVertexInputBindingStride;
	public int maxVertexOutputComponents;
	public int maxTessellationGenerationLevel;
	public int maxTessellationPatchSize;
	public int maxTessellationControlPerVertexInputComponents;
	public int maxTessellationControlPerVertexOutputComponents;
	public int maxTessellationControlPerPatchOutputComponents;
	public int maxTessellationControlTotalOutputComponents;
	public int maxTessellationEvaluationInputComponents;
	public int maxTessellationEvaluationOutputComponents;
	public int maxGeometryShaderInvocations;
	public int maxGeometryInputComponents;
	public int maxGeometryOutputComponents;
	public int maxGeometryOutputVertices;
	public int maxGeometryTotalOutputComponents;
	public int maxFragmentInputComponents;
	public int maxFragmentOutputAttachments;
	public int maxFragmentDualSrcAttachments;
	public int maxFragmentCombinedOutputResources;
	public int maxComputeSharedMemorySize;
	public int[] maxComputeWorkGroupCount = new int[3];
	public int maxComputeWorkGroupInvocations;
	public int[] maxComputeWorkGroupSize = new int[3];
	public int subPixelPrecisionBits;
	public int subTexelPrecisionBits;
	public int mipmapPrecisionBits;
	public int maxDrawIndexedIndexValue;
	public int maxDrawIndirectCount;
	public float maxSamplerLodBias;
	public float maxSamplerAnisotropy;
	public int maxViewports;
	public int[] maxViewportDimensions = new int[2];
	public float[] viewportBoundsRange = new float[2];
	public int viewportSubPixelBits;
	public long minMemoryMapAlignment;
	public long minTexelBufferOffsetAlignment;
	public long minUniformBufferOffsetAlignment;
	public long minStorageBufferOffsetAlignment;
	public int minTexelOffset;
	public int maxTexelOffset;
	public int minTexelGatherOffset;
	public int maxTexelGatherOffset;
	public float minInterpolationOffset;
	public float maxInterpolationOffset;
	public int subPixelInterpolationOffsetBits;
	public int maxFramebufferWidth;
	public int maxFramebufferHeight;
	public int maxFramebufferLayers;
	public int framebufferColorSampleCounts;
	public int framebufferDepthSampleCounts;
	public int framebufferStencilSampleCounts;
	public int framebufferNoAttachmentsSampleCounts;
	public int maxColorAttachments;
	public int sampledImageColorSampleCounts;
	public int sampledImageIntegerSampleCounts;
	public int sampledImageDepthSampleCounts;
	public int sampledImageStencilSampleCounts;
	public int storageImageSampleCounts;
	public int maxSampleMaskWords;
	public VulkanBoolean timestampComputeAndGraphics;
	public float timestampPeriod;
	public int maxClipDistances;
	public int maxCullDistances;
	public int maxCombinedClipAndCullDistances;
	public int discreteQueuePriorities;
	public float[] pointSizeRange = new float[2];
	public float[] lineWidthRange = new float[2];
	public float pointSizeGranularity;
	public float lineWidthGranularity;
	public VulkanBoolean strictLines;
	public VulkanBoolean standardSampleLocations;
	public long optimalBufferCopyOffsetAlignment;
	public long optimalBufferCopyRowPitchAlignment;
	public long nonCoherentAtomSize;
}
