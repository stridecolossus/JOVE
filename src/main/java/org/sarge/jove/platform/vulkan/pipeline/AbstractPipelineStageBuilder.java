package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.common.VulkanStructure;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;

/**
 * Base-class for a nested pipeline stage builder.
 * @param <R> Return type
 * @param <T> Builder type
 * @author Sarge
 */
abstract class AbstractPipelineStageBuilder<R extends VulkanStructure, T extends AbstractPipelineStageBuilder<R, T>> {
	private Builder parent;

	/**
	 * Copies from the given builder.
	 * @param builder Builder to copy
	 */
	abstract void copy(T builder);

	/**
	 * @return Result of this builder
	 */
	abstract R get();

	/**
	 * Sets the parent of this builder.
	 * @param parent Parent builder
	 */
	final void parent(Builder parent) {
		this.parent = notNull(parent);
	}

	/**
	 * Constructs this object.
	 * @return Parent pipeline builder
	 */
	public final Builder build() {
		return parent;
	}
}
