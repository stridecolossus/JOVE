package org.sarge.jove.platform.vulkan.pipeline;

import org.sarge.jove.platform.vulkan.pipeline.Pipeline.Builder;

/**
 * Base-class for a pipeline stage builder.
 * TODO
 * @author Sarge
 */
abstract class AbstractPipelineStageBuilder {
	/**
	 * Completes construction.
	 * @throws UnsupportedOperationException by default
	 */
	public Builder build() {
		throw new UnsupportedOperationException();
	}
}
