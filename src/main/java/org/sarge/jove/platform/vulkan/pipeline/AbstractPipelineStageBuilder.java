package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.util.Check.notNull;

/**
 * Base-class for a pipeline stage builder.
 * @param <T> Return type
 * @author Sarge
 */
abstract class AbstractPipelineStageBuilder<T> {
	private Pipeline.Builder parent;

	/**
	 * Sets the parent builder.
	 * @param parent Parent builder
	 */
	protected void parent(Pipeline.Builder parent) {
		this.parent = notNull(parent);
	}

	/**
	 * @return Result of this builder
	 */
	protected abstract T result();

	/**
	 * Completes construction.
	 */
	public Pipeline.Builder build() {
		return parent;
	}
}
