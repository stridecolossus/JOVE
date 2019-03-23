package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.RenderingService;

/**
 * Vulkan implementation.
 * @author Sarge
 */
public class VulkanRenderingSystem implements RenderingService {
	private final VulkanInstance instance;

	/**
	 * Constructor.
	 * @param instance Vulkan instance
	 */
	public VulkanRenderingSystem(VulkanInstance instance) {
		this.instance = notNull(instance);
	}

	@Override
	public String name() {
		return "Vulkan";
	}

	@Override
	public void handler(ErrorHandler handler) {
		// TODO
	}

	@Override
	public String version() {
		return VulkanLibrary.VERSION.toString();
	}

	@Override
	public void close() {
		instance.destroy();
	}
}
