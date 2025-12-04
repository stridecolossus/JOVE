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
	public GroupLayout layout() {
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
