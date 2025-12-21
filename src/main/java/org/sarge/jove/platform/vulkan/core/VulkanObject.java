package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.common.*;

/**
 * A <i>Vulkan object</i> is a template base-class for objects derived from the logical device.
 * @author Sarge
 */
public abstract class VulkanObject extends TransientNativeObject {
	private final LogicalDevice device;

	/**
	 * Constructor.
	 * @param handle		Object handle
	 * @param device		Logical device
	 */
	protected VulkanObject(Handle handle, LogicalDevice device) {
		super(handle);
		this.device = requireNonNull(device);
	}

	/**
	 * @return Logical device
	 */
	public LogicalDevice device() {
		return device;
	}

	/**
	 * A <i>destructor</i> abstracts the API method used to destroy this object.
	 * @param <T> Type
	 * @see VulkanObject#destructor()
	 */
	@FunctionalInterface
	protected interface Destructor<T extends VulkanObject> {
		/**
		 * Destroys this object.
		 * @param device		Logical device
		 * @param object		Native object to destroy
		 * @param allocator		Vulkan memory allocator, always {@code null}
		 */
		void destroy(LogicalDevice device, T object, Handle allocator);
	}

	/**
	 * Provides the <i>destructor</i> API method for this object.
	 * @param lib Vulkan API
	 * @return Destructor method
	 */
	protected abstract Destructor<?> destructor();

	@SuppressWarnings("unchecked")
	@Override
	public void destroy() {
		@SuppressWarnings("rawtypes")
		final Destructor destructor = this.destructor();
		destructor.destroy(device, this, null);
		super.destroy();
	}

	@Override
	protected void release() {
		// Does nowt
	}
}
