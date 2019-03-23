package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.Handle;

import com.sun.jna.Pointer;

/**
 * A <i>Vulkan handle</i> is an adapter for a Vulkan resource.
 * <p>
 * The use of a <i>destructor</i> is introduced to simplify Vulkan resources since the collaborators required to destroy a resource are generally the same as those used to create one.
 * This pattern is intended to avoid having to store these dependencies in the resource itself purely to support the destruction method.
 * <p>
 * Notes:
 * <ul>
 * <li>the {@link #destroy()} method is <tt>final</tt></li>
 * <li>{@link #cleanup()} can be over-ridden to destroy dependencies</li>
 * </ul>
 * Example:
 * <pre>
 * // Define a Vulkan resource
 * class Thing extends VulkanHandle {
 *     public Thing(Pointer handle, Pointer parent, Destructor destructor, ...) {
 *         super(handle, destructor);
 *         ...
 *     }
 *
 *     ...
 *
 *     protected void cleanup() {
 *         // Destroy dependencies here
 *         ...
 *     }
 * }
 *
 * // Create native object
 * final Pointer ptr = lib.vkCreateThing(...);
 *
 * // Create thing
 * final Destructor destructor = () -> lib.vkDestroyThing(handle);
 * final Thing thing = new Thing(ptr, destructor, ...);
 * </pre>
 * @author Sarge
 */
class VulkanHandle extends Handle {
	/**
	 * Destructor method for a Vulkan resource.
	 */
	public interface Destructor {
		/**
		 * Destroys this resource.
		 */
		void destroy();

		/**
		 * Destructor that does nothing.
		 */
		Destructor NULL = () -> {
			// Does nowt
		};
	}

	private final Destructor destructor;

	/**
	 * Constructor.
	 * @param handle		Native handle
	 * @param destructor	Destructor method
	 */
	protected VulkanHandle(Pointer handle, Destructor destructor) {
		super(handle);
		this.destructor = notNull(destructor);
	}

	/**
	 * Copy constructor.
	 * @param handle Handle
	 */
	protected VulkanHandle(VulkanHandle handle) {
		this(handle.handle(), handle.destructor);
	}

	@Override
	public final synchronized void destroy() {
		cleanup();
		destructor.destroy();
		super.destroy();
	}

	/**
	 * Destroys any dependent resources created by this object.
	 * Default implementation does nothing.
	 */
	protected void cleanup() {
		// Does nowt
	}
}
