package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.lib.util.Check.notNull;

/**
 * Base-class for a nested builder.
 * @param <T> Return type
 * @author Sarge
 */
abstract class AbstractPipelineBuilder<T> {
	private Pipeline.Builder parent;

	/**
	 * Sets the parent builder.
	 * @param parent Parent
	 */
	void parent(Pipeline.Builder parent) {
		this.parent = notNull(parent);
	}

	/**
	 * @return Result of this builder
	 */
	abstract T get();

	/**
	 * Constructs this object.
	 * @return Pipeline builder
	 */
	public Pipeline.Builder build() {
		return parent;
	}
}
