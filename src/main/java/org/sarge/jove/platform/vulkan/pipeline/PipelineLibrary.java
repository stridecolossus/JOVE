package org.sarge.jove.platform.vulkan.pipeline;

/**
 * Aggregated pipeline API.
 * @author Sarge
 */
public interface PipelineLibrary extends Pipeline.Library, PipelineLayout.Library, /* PipelineCache.Library, DynamicStateStage.Library,*/ Shader.Library {
	// Aggregate interface
}
