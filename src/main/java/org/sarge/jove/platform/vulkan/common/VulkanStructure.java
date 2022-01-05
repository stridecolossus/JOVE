package org.sarge.jove.platform.vulkan.common;

import java.util.List;

import org.sarge.jove.platform.vulkan.core.VulkanLibrary;

import com.sun.jna.Structure;

/**
 * Base-class for all Vulkan structures.
 * @author Sarge
 */
public abstract class VulkanStructure extends Structure {
	/**
	 * Constructor.
	 */
	protected VulkanStructure() {
		super(VulkanLibrary.MAPPER);
	}

	@Override
	public List<String> getFieldOrder() {
		return super.getFieldOrder();
	}

	/**
	 * Clones this structure.
	 * @param <T> Structure type
	 * @return New cloned structure
	 * @throws RuntimeException if the structure cannot be copied
	 * @throws ClassCastException if the return type does not match the sub-class of this structure
	 */
	public <T extends VulkanStructure> T copy() {
		// Create copy
		@SuppressWarnings("unchecked")
		final T copy = (T) Structure.newInstance(this.getClass());

		// Init this structure
		write();

		// Read backing data
		final int size = this.size();
		final byte[] data = getPointer().getByteArray(0, size);

		// Write to copy
		copy.getPointer().write(0, data, 0, size);
		copy.read();

		return copy;
	}
}
