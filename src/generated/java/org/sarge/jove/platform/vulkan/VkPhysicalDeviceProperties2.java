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
public class VkPhysicalDeviceProperties2 implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkPhysicalDeviceProperties properties;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
				JAVA_INT.withName("apiVersion"),
				JAVA_INT.withName("driverVersion"),
				JAVA_INT.withName("vendorID"),
				JAVA_INT.withName("deviceID"),
				JAVA_INT.withName("deviceType"),
				MemoryLayout.sequenceLayout(256, JAVA_CHAR).withName("deviceName"),
				MemoryLayout.sequenceLayout(16, JAVA_BYTE).withName("pipelineCacheUUID"),
				PADDING,
				MemoryLayout.structLayout(
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
				).withName("limits"),
				MemoryLayout.structLayout(
					JAVA_INT.withName("residencyStandard2DBlockShape"),
					JAVA_INT.withName("residencyStandard2DMultisampleBlockShape"),
					JAVA_INT.withName("residencyStandard3DBlockShape"),
					JAVA_INT.withName("residencyAlignedMipSize"),
					JAVA_INT.withName("residencyNonResidentStrict")
				).withName("sparseProperties"),
				PADDING
			).withName("properties")
		);
	}
}
