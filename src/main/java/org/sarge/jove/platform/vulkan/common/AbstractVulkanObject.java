package org.sarge.jove.platform.vulkan.common;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;

import com.sun.jna.Pointer;

/**
 * An <i>abstract Vulkan object</i> is a template base-class for an object derived from the {@link DeviceContext}.
 * @author Sarge
 */
public abstract class AbstractVulkanObject extends AbstractTransientNativeObject {
	/**
	 * A <i>destructor</i> abstracts the API method used to destroy this object.
	 * @see AbstractVulkanObject#destructor(VulkanLibrary)
	 */
	@FunctionalInterface
	public interface Destructor {
		/**
		 * Destroys this object.
		 * @param dev			Logical device
		 * @param handle		Handle
		 * @param allocator		Vulkan memory allocator (always {@code null})
		 */
		void destroy(Handle dev, Handle handle, Handle allocator);
	}

	private final DeviceContext dev;

	/**
	 * Constructor.
	 * @param handle		Object handle
	 * @param dev			Device context
	 */
	protected AbstractVulkanObject(Pointer handle, DeviceContext dev) {
		super(new Handle(handle));
		this.dev = notNull(dev);
	}

	/**
	 * @return Device context
	 */
	public DeviceContext device() {
		return dev;
	}

	/**
	 * Provides the <i>destructor</i> API method for this object.
	 * @param lib Vulkan API
	 * @return Destructor method
	 */
	protected abstract Destructor destructor(VulkanLibrary lib);

	@Override
	public final void destroy() {
		// Destroy this object
		final Destructor destructor = destructor(dev.library());
		destructor.destroy(dev.handle(), this.handle(), null);

		// Delegate
		super.destroy();
	}

	@Override
	protected void release() {
		// Does nowt
	}
}
