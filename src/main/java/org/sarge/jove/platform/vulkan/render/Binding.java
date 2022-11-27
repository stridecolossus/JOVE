package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.BitMask;
import org.sarge.lib.util.Check;

/**
 * A <i>binding</i> defines the properties of a descriptor set layout.
 * @author Sarge
 */
public record Binding(int index, VkDescriptorType type, int count, Set<VkShaderStage> stages) {
	/**
	 * Constructor.
	 * @param index			Binding index
	 * @param type			Descriptor type
	 * @param count			Array size
	 * @param stages		Pipeline stage flags
	 * @throws IllegalArgumentException if the pipeline {@link #stages} is empty
	 */
	public Binding {
		if(stages.isEmpty()) throw new IllegalArgumentException("No pipeline stages specified for binding");
		Check.zeroOrMore(index);
		Check.notNull(type);
		Check.oneOrMore(count);
		stages = Set.copyOf(stages);
	}

	/**
	 * Populates a binding descriptor.
	 */
	void populate(VkDescriptorSetLayoutBinding info) {
		info.binding = index;
		info.descriptorType = type;
		info.descriptorCount = count;
		info.stageFlags = BitMask.reduce(stages);
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
		 * Sets the index of this binding (default is binding zero).
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
		 * Sets the array count of this binding (default is one).
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
		 * @see Binding#Binding(int, VkDescriptorType, int, Set)
		 */
		public Binding build() {
			return new Binding(binding, type, count, stages);
		}
	}
}
