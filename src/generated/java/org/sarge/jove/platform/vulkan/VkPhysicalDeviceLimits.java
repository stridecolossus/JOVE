package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;
import org.sarge.jove.common.Handle;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.platform.vulkan.*;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPhysicalDeviceLimits implements NativeStructure {
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
	public int[] maxComputeWorkGroupCount;
	public int maxComputeWorkGroupInvocations;
	public int[] maxComputeWorkGroupSize;
	public int subPixelPrecisionBits;
	public int subTexelPrecisionBits;
	public int mipmapPrecisionBits;
	public int maxDrawIndexedIndexValue;
	public int maxDrawIndirectCount;
	public float maxSamplerLodBias;
	public float maxSamplerAnisotropy;
	public int maxViewports;
	public int[] maxViewportDimensions;
	public float[] viewportBoundsRange;
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
	public EnumMask<VkSampleCountFlags> framebufferColorSampleCounts;
	public EnumMask<VkSampleCountFlags> framebufferDepthSampleCounts;
	public EnumMask<VkSampleCountFlags> framebufferStencilSampleCounts;
	public EnumMask<VkSampleCountFlags> framebufferNoAttachmentsSampleCounts;
	public int maxColorAttachments;
	public EnumMask<VkSampleCountFlags> sampledImageColorSampleCounts;
	public EnumMask<VkSampleCountFlags> sampledImageIntegerSampleCounts;
	public EnumMask<VkSampleCountFlags> sampledImageDepthSampleCounts;
	public EnumMask<VkSampleCountFlags> sampledImageStencilSampleCounts;
	public EnumMask<VkSampleCountFlags> storageImageSampleCounts;
	public int maxSampleMaskWords;
	public boolean timestampComputeAndGraphics;
	public float timestampPeriod;
	public int maxClipDistances;
	public int maxCullDistances;
	public int maxCombinedClipAndCullDistances;
	public int discreteQueuePriorities;
	public float[] pointSizeRange;
	public float[] lineWidthRange;
	public float pointSizeGranularity;
	public float lineWidthGranularity;
	public boolean strictLines;
	public boolean standardSampleLocations;
	public long optimalBufferCopyOffsetAlignment;
	public long optimalBufferCopyRowPitchAlignment;
	public long nonCoherentAtomSize;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("maxImageDimension1D"),
			JAVA_INT.withName("maxImageDimension2D"),
			JAVA_INT.withName("maxImageDimension3D"),
			JAVA_INT.withName("maxImageDimensionCube"),
			JAVA_INT.withName("maxImageArrayLayers"),
			JAVA_INT.withName("maxTexelBufferElements"),
			JAVA_INT.withName("maxUniformBufferRange"),
			JAVA_INT.withName("maxStorageBufferRange"),
			JAVA_INT.withName("maxPushConstantsSize"),
			JAVA_INT.withName("maxMemoryAllocationCount"),
			JAVA_INT.withName("maxSamplerAllocationCount"),
			PADDING,
			JAVA_LONG.withName("bufferImageGranularity"),
			JAVA_LONG.withName("sparseAddressSpaceSize"),
			JAVA_INT.withName("maxBoundDescriptorSets"),
			JAVA_INT.withName("maxPerStageDescriptorSamplers"),
			JAVA_INT.withName("maxPerStageDescriptorUniformBuffers"),
			JAVA_INT.withName("maxPerStageDescriptorStorageBuffers"),
			JAVA_INT.withName("maxPerStageDescriptorSampledImages"),
			JAVA_INT.withName("maxPerStageDescriptorStorageImages"),
			JAVA_INT.withName("maxPerStageDescriptorInputAttachments"),
			JAVA_INT.withName("maxPerStageResources"),
			JAVA_INT.withName("maxDescriptorSetSamplers"),
			JAVA_INT.withName("maxDescriptorSetUniformBuffers"),
			JAVA_INT.withName("maxDescriptorSetUniformBuffersDynamic"),
			JAVA_INT.withName("maxDescriptorSetStorageBuffers"),
			JAVA_INT.withName("maxDescriptorSetStorageBuffersDynamic"),
			JAVA_INT.withName("maxDescriptorSetSampledImages"),
			JAVA_INT.withName("maxDescriptorSetStorageImages"),
			JAVA_INT.withName("maxDescriptorSetInputAttachments"),
			JAVA_INT.withName("maxVertexInputAttributes"),
			JAVA_INT.withName("maxVertexInputBindings"),
			JAVA_INT.withName("maxVertexInputAttributeOffset"),
			JAVA_INT.withName("maxVertexInputBindingStride"),
			JAVA_INT.withName("maxVertexOutputComponents"),
			JAVA_INT.withName("maxTessellationGenerationLevel"),
			JAVA_INT.withName("maxTessellationPatchSize"),
			JAVA_INT.withName("maxTessellationControlPerVertexInputComponents"),
			JAVA_INT.withName("maxTessellationControlPerVertexOutputComponents"),
			JAVA_INT.withName("maxTessellationControlPerPatchOutputComponents"),
			JAVA_INT.withName("maxTessellationControlTotalOutputComponents"),
			JAVA_INT.withName("maxTessellationEvaluationInputComponents"),
			JAVA_INT.withName("maxTessellationEvaluationOutputComponents"),
			JAVA_INT.withName("maxGeometryShaderInvocations"),
			JAVA_INT.withName("maxGeometryInputComponents"),
			JAVA_INT.withName("maxGeometryOutputComponents"),
			JAVA_INT.withName("maxGeometryOutputVertices"),
			JAVA_INT.withName("maxGeometryTotalOutputComponents"),
			JAVA_INT.withName("maxFragmentInputComponents"),
			JAVA_INT.withName("maxFragmentOutputAttachments"),
			JAVA_INT.withName("maxFragmentDualSrcAttachments"),
			JAVA_INT.withName("maxFragmentCombinedOutputResources"),
			JAVA_INT.withName("maxComputeSharedMemorySize"),
			MemoryLayout.sequenceLayout(3, JAVA_INT).withName("maxComputeWorkGroupCount"),
			JAVA_INT.withName("maxComputeWorkGroupInvocations"),
			MemoryLayout.sequenceLayout(3, JAVA_INT).withName("maxComputeWorkGroupSize"),
			JAVA_INT.withName("subPixelPrecisionBits"),
			JAVA_INT.withName("subTexelPrecisionBits"),
			JAVA_INT.withName("mipmapPrecisionBits"),
			JAVA_INT.withName("maxDrawIndexedIndexValue"),
			JAVA_INT.withName("maxDrawIndirectCount"),
			JAVA_FLOAT.withName("maxSamplerLodBias"),
			JAVA_FLOAT.withName("maxSamplerAnisotropy"),
			JAVA_INT.withName("maxViewports"),
			MemoryLayout.sequenceLayout(2, JAVA_INT).withName("maxViewportDimensions"),
			MemoryLayout.sequenceLayout(2, JAVA_FLOAT).withName("viewportBoundsRange"),
			JAVA_INT.withName("viewportSubPixelBits"),
			PADDING,
			JAVA_LONG.withName("minMemoryMapAlignment"),
			JAVA_LONG.withName("minTexelBufferOffsetAlignment"),
			JAVA_LONG.withName("minUniformBufferOffsetAlignment"),
			JAVA_LONG.withName("minStorageBufferOffsetAlignment"),
			JAVA_INT.withName("minTexelOffset"),
			JAVA_INT.withName("maxTexelOffset"),
			JAVA_INT.withName("minTexelGatherOffset"),
			JAVA_INT.withName("maxTexelGatherOffset"),
			JAVA_FLOAT.withName("minInterpolationOffset"),
			JAVA_FLOAT.withName("maxInterpolationOffset"),
			JAVA_INT.withName("subPixelInterpolationOffsetBits"),
			JAVA_INT.withName("maxFramebufferWidth"),
			JAVA_INT.withName("maxFramebufferHeight"),
			JAVA_INT.withName("maxFramebufferLayers"),
			JAVA_INT.withName("framebufferColorSampleCounts"),
			JAVA_INT.withName("framebufferDepthSampleCounts"),
			JAVA_INT.withName("framebufferStencilSampleCounts"),
			JAVA_INT.withName("framebufferNoAttachmentsSampleCounts"),
			JAVA_INT.withName("maxColorAttachments"),
			JAVA_INT.withName("sampledImageColorSampleCounts"),
			JAVA_INT.withName("sampledImageIntegerSampleCounts"),
			JAVA_INT.withName("sampledImageDepthSampleCounts"),
			JAVA_INT.withName("sampledImageStencilSampleCounts"),
			JAVA_INT.withName("storageImageSampleCounts"),
			JAVA_INT.withName("maxSampleMaskWords"),
			JAVA_INT.withName("timestampComputeAndGraphics"),
			JAVA_FLOAT.withName("timestampPeriod"),
			JAVA_INT.withName("maxClipDistances"),
			JAVA_INT.withName("maxCullDistances"),
			JAVA_INT.withName("maxCombinedClipAndCullDistances"),
			JAVA_INT.withName("discreteQueuePriorities"),
			MemoryLayout.sequenceLayout(2, JAVA_FLOAT).withName("pointSizeRange"),
			MemoryLayout.sequenceLayout(2, JAVA_FLOAT).withName("lineWidthRange"),
			JAVA_FLOAT.withName("pointSizeGranularity"),
			JAVA_FLOAT.withName("lineWidthGranularity"),
			JAVA_INT.withName("strictLines"),
			JAVA_INT.withName("standardSampleLocations"),
			PADDING,
			JAVA_LONG.withName("optimalBufferCopyOffsetAlignment"),
			JAVA_LONG.withName("optimalBufferCopyRowPitchAlignment"),
			JAVA_LONG.withName("nonCoherentAtomSize")
		);
	}
}
