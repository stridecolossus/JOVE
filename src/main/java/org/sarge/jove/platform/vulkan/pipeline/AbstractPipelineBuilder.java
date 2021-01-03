package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;

/**
 * Base-class for a nested builder.
 * @param <T> Return type
 * @author Sarge
 */
abstract class AbstractPipelineBuilder<T> {
	private Pipeline.Builder parent;

	/**
	 * Sets the parent builder.
	 * @param parent Parent builder
	 */
	protected void parent(Pipeline.Builder parent) {
		this.parent = notNull(parent);
	}
	// TODO - can we get rid of this without enforcing all the derived builders to have a ctor for the parent? proxy is too nasty

	/**
	 * @return Result of this builder
	 */
	protected abstract T result();

	/**
	 * Completes construction.
	 * @return Parent builder
	 */
	public Pipeline.Builder build() {
		return parent;
	}
}
