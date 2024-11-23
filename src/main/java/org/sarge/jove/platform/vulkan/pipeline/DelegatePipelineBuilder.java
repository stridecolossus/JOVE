package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.util.BitMask;

/**
 * A <i>delegate pipeline builder</i> is used to configure and create a pipeline.
 * @see Pipeline.Builder
 * @author Sarge
 */
public interface DelegatePipelineBuilder<T extends VulkanStructure> {
	/**
	 * @return Pipeline type
	 */
	VkPipelineBindPoint type();

	/**
	 * @return Pipeline create descriptor
	 */
	T identity();

	/**
	 * Populates the creation descriptor for this pipeline.
	 * @param flags			Creation flags
	 * @param base			Base pipeline
	 * @param parent		Parent index
	 * @param info			Pipeline create descriptor
	 */
	void populate(BitMask<VkPipelineCreateFlag> flags, PipelineLayout layout, Handle base, int parent, T info);

	/**
	 * Create an array of pipelines.
	 * @param dev			Logical device
	 * @param cache			Optional pipeline cache
	 * @param array			Pipeline create descriptors
	 * @param handles		Returned pipelines
	 * @return Result
	 */
	int create(DeviceContext dev, PipelineCache cache, T[] array, Handle[] handles);

	/**
	 * Convenience short-cut method to create a single pipeline from this builder.
	 * @param dev			Logical device
	 * @param layout		Pipeline layout
	 * @return New pipeline
	 * @see Pipeline.Builder#build(DeviceContext, PipelineLayout, PipelineCache)
	 */
	default Pipeline build(DeviceContext dev, PipelineLayout layout) {
		return new Pipeline.Builder<>(DelegatePipelineBuilder.this)
				.build(dev, layout, null)
				.iterator()
				.next();
	}
}
