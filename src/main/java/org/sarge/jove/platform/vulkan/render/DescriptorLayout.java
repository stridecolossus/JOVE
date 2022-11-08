package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>descriptor set layout</i> specifies the resource bindings for a descriptor set.
 * @author Sarge
 */
public class DescriptorLayout extends AbstractVulkanObject {
	/**
	 * A <i>binding</i> defines the properties of the descriptor sets comprising this layout.
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
			public Binding build() {
				return new Binding(binding, type, count, stages);
			}
		}
	}

	/**
	 * Creates a descriptor set layout.
	 * @param dev			Logical device
	 * @param bindings		Bindings
	 * @return New descriptor set layout
	 * @throws IllegalArgumentException if the bindings are empty or contain duplicate indices
	 */
	public static DescriptorLayout create(DeviceContext dev, Collection<Binding> bindings) {
		// Check binding indices
		final long count = bindings.stream().map(Binding::index).distinct().count();
		if(count != bindings.size()) {
			throw new IllegalArgumentException("Binding indices must be unique: " + bindings);
		}

		// Init layout descriptor
		final var info = new VkDescriptorSetLayoutCreateInfo();
		info.bindingCount = bindings.size();
		info.pBindings = StructureCollector.pointer(bindings, new VkDescriptorSetLayoutBinding(), Binding::populate);

		// Allocate layout
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = dev.factory().pointer();
		check(lib.vkCreateDescriptorSetLayout(dev, info, null, ref));

		// Create layout
		return new DescriptorLayout(new Handle(ref), dev, bindings);
	}

	private final Collection<Binding> bindings;

	/**
	 * Constructor.
	 * @param handle		Layout handle
	 * @param dev			Logical device
	 * @param bindings		Bindings
	 */
	DescriptorLayout(Handle handle, DeviceContext dev, Collection<Binding> bindings) {
		super(handle, dev);
		Check.notEmpty(bindings);
		this.bindings = List.copyOf(bindings);
	}

	/**
	 * @return Bindings
	 */
	public Collection<Binding> bindings() {
		return bindings;
	}

	@Override
	protected Destructor<DescriptorLayout> destructor(VulkanLibrary lib) {
		return lib::vkDestroyDescriptorSetLayout;
	}

	@Override
	public int hashCode() {
		return bindings.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof DescriptorLayout that) &&
				this.bindings.equals(that.bindings());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(bindings)
				.build();
	}
}
