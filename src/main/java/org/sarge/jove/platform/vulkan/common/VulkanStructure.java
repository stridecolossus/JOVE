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
	public final List<String> getFieldOrder() {
		return super.getFieldOrder();
	}
}
