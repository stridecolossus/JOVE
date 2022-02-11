package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toMap;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkDescriptorSetLayoutBinding;
import org.sarge.jove.platform.vulkan.VkDescriptorSetLayoutCreateInfo;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.util.StructureHelper;
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
	 * @throws IllegalArgumentException for if the bindings are empty
	 * @throws IllegalStateException for a duplicate binding index
	 */
	public static DescriptorLayout create(DeviceContext dev, List<ResourceBinding> bindings) {
		// Init layout descriptor
		final VkDescriptorSetLayoutCreateInfo info = new VkDescriptorSetLayoutCreateInfo();
		info.bindingCount = bindings.size();
		info.pBindings = StructureHelper.pointer(bindings, VkDescriptorSetLayoutBinding::new, ResourceBinding::populate);

		// Allocate layout
		final VulkanLibrary lib = dev.library();
		final PointerByReference handle = dev.factory().pointer();
		check(lib.vkCreateDescriptorSetLayout(dev, info, null, handle));

		// Create layout
		return new DescriptorLayout(handle.getValue(), dev, bindings);
	}

	private final Map<Integer, ResourceBinding> bindings;

	/**
	 * Constructor.
	 * @param handle		Layout handle
	 * @param dev			Logical device
	 * @param bindings		Bindings
	 * @throws IllegalStateException for a duplicate binding index
	 */
	DescriptorLayout(Pointer handle, DeviceContext dev, List<ResourceBinding> bindings) {
		super(handle, dev);
		Check.notEmpty(bindings);
		this.bindings = bindings.stream().collect(toMap(ResourceBinding::index, Function.identity()));
	}

	/**
	 * @return Bindings
	 */
	public Map<Integer, ResourceBinding> bindings() {
		return bindings;
	}

	/**
	 * Looks up a binding descriptor.
	 * @param index Binding index
	 * @return Binding
	 */
	public ResourceBinding binding(int index) {
		return bindings.get(index);
	}

	@Override
	protected Destructor<DescriptorLayout> destructor(VulkanLibrary lib) {
		return lib::vkDestroyDescriptorSetLayout;
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
				.append(bindings.values())
				.build();
	}
}
