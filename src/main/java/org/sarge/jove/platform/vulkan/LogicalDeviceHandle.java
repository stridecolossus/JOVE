package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Function;

import org.sarge.jove.platform.Handle;

import com.sun.jna.Pointer;

/**
 * A <i>logical device handle</i> is a specialized resource handle dependant on a {@link LogicalDevice}.
 * <p>
 * As well as the resource handle and <i>parent</i> device the constructor accepts a function that maps the Vulkan API to the destructor method for this object.
 * This allows sub-classes to simply pass a method reference for the destructor.
 * <pre>
 * Thing thing = new Thing(handle, dev, lib -> lib::vkDestroyThing);
 * </pre>
 * @author Sarge
 */
public class LogicalDeviceHandle extends Handle {
	/**
	 * Destructor.
	 */
	@FunctionalInterface
	public interface Destructor {
		/**
		 * Destroys this resource.
		 * @param dev			Logical device handle
		 * @param handle		Handle
		 * @param allocator		Allocator
		 */
		void destroy(Pointer dev, Pointer handle, Pointer allocator);
	}

	protected final LogicalDevice dev;

	private Destructor destructor;

	/**
	 * Constructor.
	 * @param handle		Handle
	 * @param dev			Logical device
	 * @param mapper		Destructor mapper
	 */
	protected LogicalDeviceHandle(Pointer handle, LogicalDevice dev, Function<VulkanLibrary, Destructor> mapper) {
		super(handle);
		this.dev = notNull(dev);
		this.destructor = mapper.apply(dev.parent().vulkan().library());
	}

	@Override
	public final synchronized void destroy() {
		cleanup();
		destructor.destroy(dev.handle(), super.handle(), null);
		super.destroy();
	}

	/**
	 * Releases dependant resources.
	 */
	protected void cleanup() {
		// Does nowt
	}
}
