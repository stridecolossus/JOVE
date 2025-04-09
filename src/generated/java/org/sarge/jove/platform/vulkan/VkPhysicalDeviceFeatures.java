package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_INT;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPhysicalDeviceFeatures implements NativeStructure {
	public int robustBufferAccess;
	public int fullDrawIndexUint32;
	public int imageCubeArray;
	public int independentBlend;
	public int geometryShader;
	public int tessellationShader;
	public int sampleRateShading;
	public int dualSrcBlend;
	public int logicOp;
	public int multiDrawIndirect;
	public int drawIndirectFirstInstance;
	public int depthClamp;
	public int depthBiasClamp;
	public int fillModeNonSolid;
	public int depthBounds;
	public int wideLines;
	public int largePoints;
	public int alphaToOne;
	public int multiViewport;
	public int samplerAnisotropy;
	public int textureCompressionETC2;
	public int textureCompressionASTC_LDR;
	public int textureCompressionBC;
	public int occlusionQueryPrecise;
	public int pipelineStatisticsQuery;
	public int vertexPipelineStoresAndAtomics;
	public int fragmentStoresAndAtomics;
	public int shaderTessellationAndGeometryPointSize;
	public int shaderImageGatherExtended;
	public int shaderStorageImageExtendedFormats;
	public int shaderStorageImageMultisample;
	public int shaderStorageImageReadWithoutFormat;
	public int shaderStorageImageWriteWithoutFormat;
	public int shaderUniformBufferArrayDynamicIndexing;
	public int shaderSampledImageArrayDynamicIndexing;
	public int shaderStorageBufferArrayDynamicIndexing;
	public int shaderStorageImageArrayDynamicIndexing;
	public int shaderClipDistance;
	public int shaderCullDistance;
	public int shaderFloat64;
	public int shaderInt64;
	public int shaderInt16;
	public int shaderResourceResidency;
	public int shaderResourceMinLod;
	public int sparseBinding;
	public int sparseResidencyBuffer;
	public int sparseResidencyImage2D;
	public int sparseResidencyImage3D;
	public int sparseResidency2Samples;
	public int sparseResidency4Samples;
	public int sparseResidency8Samples;
	public int sparseResidency16Samples;
	public int sparseResidencyAliased;
	public int variableMultisampleRate;
	public int inheritedQueries;

	// TODO - these should be boolean values
	// VK type is VkBool which is 4 bytes, so has to be a JAVA_INT
	// framework cannot cast from int -> bool

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_INT.withName("robustBufferAccess"),
				JAVA_INT.withName("fullDrawIndexUint32"),
				JAVA_INT.withName("imageCubeArray"),
				JAVA_INT.withName("independentBlend"),
				JAVA_INT.withName("geometryShader"),
				JAVA_INT.withName("tessellationShader"),
				JAVA_INT.withName("sampleRateShading"),
				JAVA_INT.withName("dualSrcBlend"),
				JAVA_INT.withName("logicOp"),
				JAVA_INT.withName("multiDrawIndirect"),
				JAVA_INT.withName("drawIndirectFirstInstance"),
				JAVA_INT.withName("depthClamp"),
				JAVA_INT.withName("depthBiasClamp"),
				JAVA_INT.withName("fillModeNonSolid"),
				JAVA_INT.withName("depthBounds"),
				JAVA_INT.withName("wideLines"),
				JAVA_INT.withName("largePoints"),
				JAVA_INT.withName("alphaToOne"),
				JAVA_INT.withName("multiViewport"),
				JAVA_INT.withName("samplerAnisotropy"),
				JAVA_INT.withName("textureCompressionETC2"),
				JAVA_INT.withName("textureCompressionASTC_LDR"),
				JAVA_INT.withName("textureCompressionBC"),
				JAVA_INT.withName("occlusionQueryPrecise"),
				JAVA_INT.withName("pipelineStatisticsQuery"),
				JAVA_INT.withName("vertexPipelineStoresAndAtomics"),
				JAVA_INT.withName("fragmentStoresAndAtomics"),
				JAVA_INT.withName("shaderTessellationAndGeometryPointSize"),
				JAVA_INT.withName("shaderImageGatherExtended"),
				JAVA_INT.withName("shaderStorageImageExtendedFormats"),
				JAVA_INT.withName("shaderStorageImageMultisample"),
				JAVA_INT.withName("shaderStorageImageReadWithoutFormat"),
				JAVA_INT.withName("shaderStorageImageWriteWithoutFormat"),
				JAVA_INT.withName("shaderUniformBufferArrayDynamicIndexing"),
				JAVA_INT.withName("shaderSampledImageArrayDynamicIndexing"),
				JAVA_INT.withName("shaderStorageBufferArrayDynamicIndexing"),
				JAVA_INT.withName("shaderStorageImageArrayDynamicIndexing"),
				JAVA_INT.withName("shaderClipDistance"),
				JAVA_INT.withName("shaderCullDistance"),
				JAVA_INT.withName("shaderFloat64"),
				JAVA_INT.withName("shaderInt64"),
				JAVA_INT.withName("shaderInt16"),
				JAVA_INT.withName("shaderResourceResidency"),
				JAVA_INT.withName("shaderResourceMinLod"),
				JAVA_INT.withName("sparseBinding"),
				JAVA_INT.withName("sparseResidencyBuffer"),
				JAVA_INT.withName("sparseResidencyImage2D"),
				JAVA_INT.withName("sparseResidencyImage3D"),
				JAVA_INT.withName("sparseResidency2Samples"),
				JAVA_INT.withName("sparseResidency4Samples"),
				JAVA_INT.withName("sparseResidency8Samples"),
				JAVA_INT.withName("sparseResidency16Samples"),
				JAVA_INT.withName("sparseResidencyAliased"),
				JAVA_INT.withName("variableMultisampleRate"),
				JAVA_INT.withName("inheritedQueries")
		);
	}
}
