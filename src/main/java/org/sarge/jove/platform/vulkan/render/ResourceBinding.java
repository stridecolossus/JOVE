package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.Check;

/**
 * A <i>descriptor set binding</i> defines the resource properties for a binding in a descriptor set.
 * @author Sarge
 */
public record ResourceBinding(int index, VkDescriptorType type, int count, Set<VkShaderStage> stages) {
	/**
	 * Constructor.
	 * @param index			Binding index
	 * @param type			Descriptor type
	 * @param count			Array size
	 * @param stages		Pipeline stage flags
	 * @throws IllegalArgumentException if the pipeline {@link #stages} is empty
	 */
	public ResourceBinding {
		if(stages.isEmpty()) throw new IllegalArgumentException("No pipeline stages specified for binding");
		Check.zeroOrMore(index);
		Check.notNull(type);
		Check.oneOrMore(count);
		stages = Set.copyOf(stages);
	}

	/**
	 * Populates a layout binding descriptor.
	 */
	void populate(VkDescriptorSetLayoutBinding info) {
		info.binding = index;
		info.descriptorType = type;
		info.descriptorCount = count;
		info.stageFlags = IntegerEnumeration.reduce(stages);
	}

	/**
	 * Builder for a layout binding.
	 */
	public static class Builder {
		private int binding;
		private VkDescriptorType type;
		private int count = 1;
		private final Set<VkShaderStage> stages = new HashSet<>();

		/**
		 * Sets the index of this binding.
		 * @param binding Binding index
		 */
		public Builder binding(int binding) {
			this.binding = zeroOrMore(binding);
			return this;
		}

		/**
		 * Sets the descriptor type for this binding.
		 * @param type Descriptor type
		 */
		public Builder type(VkDescriptorType type) {
			this.type = notNull(type);
			return this;
		}

		/**
		 * Sets the array count of this binding.
		 * @param count Array count
		 */
		public Builder count(int count) {
			this.count = oneOrMore(count);
			return this;
		}

		/**
		 * Adds a shader stage to this binding.
		 * @param stage Shader stage
		 */
		public Builder stage(VkShaderStage stage) {
			stages.add(notNull(stage));
			return this;
		}

		/**
		 * Constructs this binding.
		 * @return New layout binding
		 */
		public ResourceBinding build() {
			return new ResourceBinding(binding, type, count, stages);
		}
	}
}
