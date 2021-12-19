package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;

/**
 * Base-class for a nested pipeline stage builder.
 * @param <T> Return type
 * @author Sarge
 */
abstract class AbstractPipelineStageBuilder<T> {
	private final Builder parent;

	/**
	 * Constructor.
	 * @param parent Parent builder
	 */
	protected AbstractPipelineStageBuilder(Builder parent) {
		this.parent = parent;
	}

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
