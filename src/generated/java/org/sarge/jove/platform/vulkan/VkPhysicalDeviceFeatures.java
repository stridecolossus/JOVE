package org.sarge.jove.platform.vulkan;

import static java.lang.foreign.ValueLayout.JAVA_BOOLEAN;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeStructure;

/**
 * Vulkan structure.
 * This class has been code-generated.
 */
public class VkPhysicalDeviceFeatures implements NativeStructure {
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

	@Override
	public StructLayout layout() {
		return MemoryLayout.structLayout(
				JAVA_BOOLEAN.withName("robustBufferAccess"),
				JAVA_BOOLEAN.withName("fullDrawIndexUint32"),
				JAVA_BOOLEAN.withName("imageCubeArray"),
				JAVA_BOOLEAN.withName("independentBlend"),
				JAVA_BOOLEAN.withName("geometryShader"),
				JAVA_BOOLEAN.withName("tessellationShader"),
				JAVA_BOOLEAN.withName("sampleRateShading"),
				JAVA_BOOLEAN.withName("dualSrcBlend"),
				JAVA_BOOLEAN.withName("logicOp"),
				JAVA_BOOLEAN.withName("multiDrawIndirect"),
				JAVA_BOOLEAN.withName("drawIndirectFirstInstance"),
				JAVA_BOOLEAN.withName("depthClamp"),
				JAVA_BOOLEAN.withName("depthBiasClamp"),
				JAVA_BOOLEAN.withName("fillModeNonSolid"),
				JAVA_BOOLEAN.withName("depthBounds"),
				JAVA_BOOLEAN.withName("wideLines"),
				JAVA_BOOLEAN.withName("largePoints"),
				JAVA_BOOLEAN.withName("alphaToOne"),
				JAVA_BOOLEAN.withName("multiViewport"),
				JAVA_BOOLEAN.withName("samplerAnisotropy"),
				JAVA_BOOLEAN.withName("textureCompressionETC2"),
				JAVA_BOOLEAN.withName("textureCompressionASTC_LDR"),
				JAVA_BOOLEAN.withName("textureCompressionBC"),
				JAVA_BOOLEAN.withName("occlusionQueryPrecise"),
				JAVA_BOOLEAN.withName("pipelineStatisticsQuery"),
				JAVA_BOOLEAN.withName("vertexPipelineStoresAndAtomics"),
				JAVA_BOOLEAN.withName("fragmentStoresAndAtomics"),
				JAVA_BOOLEAN.withName("shaderTessellationAndGeometryPointSize"),
				JAVA_BOOLEAN.withName("shaderImageGatherExtended"),
				JAVA_BOOLEAN.withName("shaderStorageImageExtendedFormats"),
				JAVA_BOOLEAN.withName("shaderStorageImageMultisample"),
				JAVA_BOOLEAN.withName("shaderStorageImageReadWithoutFormat"),
				JAVA_BOOLEAN.withName("shaderStorageImageWriteWithoutFormat"),
				JAVA_BOOLEAN.withName("shaderUniformBufferArrayDynamicIndexing"),
				JAVA_BOOLEAN.withName("shaderSampledImageArrayDynamicIndexing"),
				JAVA_BOOLEAN.withName("shaderStorageBufferArrayDynamicIndexing"),
				JAVA_BOOLEAN.withName("shaderStorageImageArrayDynamicIndexing"),
				JAVA_BOOLEAN.withName("shaderClipDistance"),
				JAVA_BOOLEAN.withName("shaderCullDistance"),
				JAVA_BOOLEAN.withName("shaderFloat64"),
				JAVA_BOOLEAN.withName("shaderInt64"),
				JAVA_BOOLEAN.withName("shaderInt16"),
				JAVA_BOOLEAN.withName("shaderResourceResidency"),
				JAVA_BOOLEAN.withName("shaderResourceMinLod"),
				JAVA_BOOLEAN.withName("sparseBinding"),
				JAVA_BOOLEAN.withName("sparseResidencyBuffer"),
				JAVA_BOOLEAN.withName("sparseResidencyImage2D"),
				JAVA_BOOLEAN.withName("sparseResidencyImage3D"),
				JAVA_BOOLEAN.withName("sparseResidency2Samples"),
				JAVA_BOOLEAN.withName("sparseResidency4Samples"),
				JAVA_BOOLEAN.withName("sparseResidency8Samples"),
				JAVA_BOOLEAN.withName("sparseResidency16Samples"),
				JAVA_BOOLEAN.withName("sparseResidencyAliased"),
				JAVA_BOOLEAN.withName("variableMultisampleRate"),
				JAVA_BOOLEAN.withName("inheritedQueries")
		);
	}
}
