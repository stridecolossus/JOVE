package org.sarge.jove.platform.vulkan.core;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.NativeObject;

import com.sun.jna.Pointer;

/**
 * Convenience base-class for a Vulkan object derived from the logical device.
 * @author Sarge
 */
public abstract class AbstractVulkanObject extends AbstractTransientNativeObject {
	/**
	 * Destructor method.
	 */
	@FunctionalInterface
	public interface Destructor {
		/**
		 * Destroys this object.
		 * @param dev			Logical device
		 * @param handle		Handle
		 * @param allocator		Allocator
		 */
		void destroy(Handle dev, Handle handle, Handle allocator);
	}

	/**
	 * Helper - Extracts the handle from the given optional object.
	 * @param obj Native object
	 * @return Handle of the given object or {@code null}
	 */
	protected static Handle handle(NativeObject obj) {
		if(obj == null) {
			return null;
		}
		else {
			return obj.handle();
		}
	}

	private final LogicalDevice dev;
	private final Destructor destructor;

	/**
	 * Constructor.
	 * @param handle		JNA pointer handle
	 * @param dev			Parent logical device
	 * @param destructor	Destructor API method
	 */
	protected AbstractVulkanObject(Pointer handle, LogicalDevice dev, Destructor destructor) {
		super(new Handle(handle));
		this.dev = notNull(dev);
		this.destructor = notNull(destructor);
	}

	/**
	 * @return Parent logical device
	 */
	public LogicalDevice device() {
		return dev;
	}

	@Override
	protected void release() {
		destructor.destroy(dev.handle(), this.handle(), null);
	}
}
