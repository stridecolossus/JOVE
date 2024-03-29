package org.sarge.jove.platform.vulkan.pipeline;

/**
 * Vulkan pipeline API.
 */
public interface PipelineLibrary extends Pipeline.Library, PipelineLayout.Library, PipelineCache.Library, DynamicStateStageBuilder.Library, Shader.Library {
	// Aggregate interface
}
