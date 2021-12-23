package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;

/**
 * Base-class for a nested pipeline stage builder.
 * @param <T> Return type
 * @author Sarge
 */
abstract class AbstractPipelineStageBuilder<T, B> {
	private Builder parent;

	/**
	 * Sets the parent builder.
	 * @param parent Parent builder
	 */
	protected void parent(Builder parent) {
		this.parent = parent;
	}

	/**
	 * Clones the properties of this builder.
	 * @param builder Builder to clone
	 */
	abstract void init(B builder);

	/**
	 * @return Result of this builder
	 */
	abstract T get();

	/**
	 * Constructs this object.
	 * @return Parent pipeline builder
	 */
	public final Builder build() {
		return parent;
	}
}
