package org.sarge.jove.platform.vulkan.render;

import java.util.List;

import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanBuffer;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;

/**
 * A <i>render sequence</i> records the command sequence for a frame.
 * @author Sarge
 */
public interface RenderSequence {
	/**
	 * Records a command sequence to the given buffer.
	 * @param index		Frame index
	 * @param buffer	Command buffer to record
	 */
	void build(int index, Buffer buffer);

	class Builder {

		public Builder pipeline(Pipeline pipeline) {
			return this;
		}

		public Builder descriptors(List<DescriptorSet> sets) {
			return this;
		}

		public Builder vertices(VulkanBuffer vertices) {
			return this;
		}

		public Builder index(VulkanBuffer index) {
			return this;
		}

		public Builder draw(DrawCommand draw) {
			return this;
		}

		public RenderSequence build() {
			return null;
		}
	}
}
