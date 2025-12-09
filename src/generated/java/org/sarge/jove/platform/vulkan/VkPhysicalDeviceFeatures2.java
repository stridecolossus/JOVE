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
public class VkPhysicalDeviceFeatures2 implements NativeStructure {
	public VkStructureType sType;
	public Handle pNext;
	public VkPhysicalDeviceFeatures features;

	@Override
	public GroupLayout layout() {
		return MemoryLayout.structLayout(
			JAVA_INT.withName("sType"),
			PADDING,
			POINTER.withName("pNext"),
			MemoryLayout.structLayout(
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
			).withName("features"),
			PADDING
		);
	}
}
