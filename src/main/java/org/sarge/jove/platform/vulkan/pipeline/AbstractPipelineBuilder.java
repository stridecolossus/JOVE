package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;

/**
 * Base-class for a nested builder.
 * @param <T> Return type
 * @author Sarge
 */
abstract class AbstractPipelineBuilder<T> {
	private Builder parent;

	/**
	 * Sets the parent builder.
	 * @param parent Parent
	 */
	void parent(Builder parent) {
		this.parent = notNull(parent);
	}

	/**
	 * @return Result of this builder
	 */
	abstract T get();

	/**
	 * Constructs this object.
	 * @return Parent pipeline builder
	 */
	public Builder build() {
		return parent;
	}
}
