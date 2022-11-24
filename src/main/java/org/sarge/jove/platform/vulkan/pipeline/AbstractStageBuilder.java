package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;

/**
 * Base-class for a nested graphics pipeline stage builder.
 * @param <R> Return type
 * @author Sarge
 */
abstract class AbstractStageBuilder<R extends VulkanStructure> {
	private GraphicsPipelineBuilder parent;

	/**
	 * @return Result of this builder
	 */
	abstract R get();

	/**
	 * Sets the parent of this builder.
	 * @param parent Parent builder
	 */
	final void parent(GraphicsPipelineBuilder parent) {
		this.parent = notNull(parent);
	}

	/**
	 * Constructs this object.
	 * @return Parent pipeline builder
	 */
	public final GraphicsPipelineBuilder build() {
		return parent;
	}
}
