package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.StructureCollector;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>descriptor set layout</i> specifies the resource bindings for a descriptor set.
 * @author Sarge
 */
public class DescriptorLayout extends AbstractVulkanObject {
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

	/**
	 * Descriptor set layout API.
	 */
	interface Library {
		/**
		 * Creates a descriptor set layout.
		 * @param device				Logical device
		 * @param pCreateInfo			Create descriptor
		 * @param pAllocator			Allocator
		 * @param pSetLayout			Returned layout handle
		 * @return Result
		 */
		int vkCreateDescriptorSetLayout(DeviceContext device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSetLayout);

		/**
		 * Destroys a descriptor set layout.
		 * @param device				Logical device
		 * @param descriptorSetLayout	Layout
		 * @param pAllocator			Allocator
		 */
		void vkDestroyDescriptorSetLayout(DeviceContext device, DescriptorLayout descriptorSetLayout, Pointer pAllocator);
	}
}
