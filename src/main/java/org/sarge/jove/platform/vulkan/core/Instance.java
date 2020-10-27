package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject.TransientNativeObject;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.api.Version;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.Check;

import com.sun.jna.Function;
import com.sun.jna.Library;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>instance</i> is the root object for a Vulkan application.
 * @author Sarge
 */
public class Instance implements TransientNativeObject {
	private final Handle handle;
	private final VulkanLibrary lib;

	private HandlerManager manager;

	/**
	 * Constructor.
	 * @param lib			Vulkan library
	 * @param handle		Instance handle
	 */
	private Instance(VulkanLibrary lib, Pointer handle) {
		this.handle = new Handle(handle);
		this.lib = notNull(lib);
	}

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Vulkan library
	 */
	VulkanLibrary library() {
		return lib;
	}

	/**
	 * Looks up a Vulkan function by name.
	 * @param name Function name
	 * @return Vulkan function
	 * @throws ServiceException if the function cannot be found
	 */
	public Function function(String name) {
		final Pointer ptr = lib.vkGetInstanceProcAddr(handle, name);
		if(ptr == null) throw new RuntimeException("Cannot find function pointer: " + name);
		return Function.getFunction(ptr);
	}

	/**
	 * The <i>handler manager</i> is a lazily instantiated local helper class used to manage message handlers attached to this instance.
	 * @see <a href="https://www.lunarg.com/wp-content/uploads/2018/05/Vulkan-Debug-Utils_05_18_v1.pdf">Vulkan-Debug-Utils_05_18_v1.pdf</a>
	 */
	public class HandlerManager {
		private final Map<MessageHandler, Pointer> handlers = new HashMap<>();
		private final Function create;
		private final Function destroy;

		private HandlerManager() {
			this.create = function("vkCreateDebugUtilsMessengerEXT");
			this.destroy = function("vkDestroyDebugUtilsMessengerEXT");
		}

		/**
		 * Adds a message handler to this instance.
		 * @param handler Message handler
		 * @throws IllegalArgumentException if the handler is already attached
		 */
		public void add(MessageHandler handler) {
			// Check handler is valid
			Check.notNull(handler);
			if(handlers.containsKey(handler)) throw new IllegalArgumentException("Duplicate message handler: " + handler);

			// Create handler
			final VkDebugUtilsMessengerCreateInfoEXT info = handler.create();
			final PointerByReference handle = lib.factory().pointer();
			final Object[] args = {Instance.this.handle, info, null, handle};
			final Object result = create.invoke(Integer.TYPE, args, options());
			check((int) result);

			// Register handler
			handlers.put(handler, handle.getValue());
		}

		/**
		 * Removes a message handler.
		 * @param handler Handler to remove
		 * @throws IllegalArgumentException if the handler is not attached
		 */
		public void remove(MessageHandler handler) {
			if(!handlers.containsKey(handler)) throw new IllegalArgumentException("Handler not present: " + handler);
			final Pointer handle = handlers.remove(handler);
			destroy(handle);
		}

		/**
		 * Helper - Destroys a message handler.
		 */
		private void destroy(Pointer handle) {
			final Object[] args = new Object[]{Instance.this.handle, handle, null};
			destroy.invoke(Void.class, args, options());
		}

		/**
		 * Destroys all message handlers.
		 */
		private void destroy() {
			handlers.values().forEach(this::destroy);
			handlers.clear();
		}

//		/**
//		 * Creates a message handler.
//		 * @param handler Message handler descriptor
//		 * @return Handle
//		 */
//		private Pointer create(MessageHandler handler) {
//			final VkDebugUtilsMessengerCreateInfoEXT info = handler.create();
//			final PointerByReference handle = lib.factory().pointer();
//			final Object[] args = {Instance.this.handle, info, null, handle};
//			create.invoke(Integer.TYPE, args, options());
//			return handle.getValue();
//		}

//		/**
//		 * Destroys a message handler.
//		 * @param handle Handle
//		 */
//		private void destroy(Pointer handle) {
//			final Object[] args = new Object[]{Instance.this.handle, handle, null};
//			destroy.invoke(Void.class, args, options());
//		}

		/**
		 * @return Type converter options
		 */
		private Map<String, ?> options() {
			return Map.of(Library.OPTION_TYPE_MAPPER, VulkanLibrary.MAPPER);
		}
	}

	/**
	 * @return Message handler manager for this instance
	 */
	public synchronized HandlerManager handlers() {
		if(manager == null) {
			manager = new HandlerManager();
		}

		return manager;
	}

//	/**
//	 * Adds a diagnostics message handler to this instance.
//	 * @param handler Message handler
//	 * @return Handle
//	 * @throws IllegalArgumentException if the handler has already been added to this instance
//	 * @throws ServiceException if the handler cannot be created
//	 */
//	public synchronized Pointer add(MessageHandler handler) {
//		// Check handler is valid
//		Check.notNull(handler);
//		if(handlers.containsKey(handler)) throw new IllegalArgumentException("Duplicate message handler: " + handler);
//
//		// Init factory
//		if(factory == null) {
//			factory = new HandlerFactory();
//		}
//
//		// Create and register handler
//		final Pointer handle = factory.create(handler);
//		handlers.put(handler, handle);
//
//		return handle;
//	}
//
//	/**
//	 * Removes (and destroys) a diagnostics message handler from this instance.
//	 * @param handler Message handler to remove
//	 * @throws IllegalArgumentException if the handler is not present or has already been removed
//	 * @throws ServiceException if the handler cannot be destroyed
//	 */
//	public synchronized void remove(MessageHandler handler) {
//		if(!handlers.containsKey(handler)) throw new IllegalArgumentException("Handler not present: " + handler);
//		final Pointer handle = handlers.remove(handler);
//		factory.destroy(handle);
//	}

	/**
	 * Destroys this instance and any active message handlers.
	 */
	@Override
	public synchronized void destroy() {
		// Destroy active handlers
		if(manager != null) {
			manager.destroy();
		}
//		if(!handlers.isEmpty()) {
//			handlers.values().forEach(factory::destroy);
//			handlers.clear();
//		}

		// Destroy instance
		lib.vkDestroyInstance(handle, null);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("handle", handle).build();
	}

	/**
	 * Builder for a Vulkan instance.
	 */
	public static class Builder {
		private VulkanLibrary lib;
		private String name;
		private Version ver = new Version(1, 0, 0);
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();

		/**
		 * Sets the Vulkan API.
		 * @param api Vulkan API
		 */
		public Builder vulkan(VulkanLibrary api) {
			this.lib = notNull(api);
			return this;
		}

		/**
		 * Sets the application name.
		 * @param name Application name
		 */
		public Builder name(String name) {
			this.name = notEmpty(name);
			return this;
		}

		/**
		 * Sets the application version.
		 * @param ver Application version
		 */
		public Builder version(Version ver) {
			this.ver = notNull(ver);
			return this;
		}

		/**
		 * Registers a required extension.
		 * @param ext Extension name
		 */
		public Builder extension(String ext) {
			Check.notEmpty(ext);
			extensions.add(ext);
			return this;
		}

		/**
		 * Registers a set of extensions.
		 * @param extensions Extension names
		 * @return
		 */
		public Builder extensions(String[] extensions) {
			Arrays.stream(extensions).forEach(this::extension);
			return this;
		}

		/**
		 * Registers a required validation layer.
		 * @param layer Validation layer descriptor
		 */
		public Builder layer(ValidationLayer layer) {
			Check.notNull(layer);
			layers.add(layer.name());
			return this;
		}

		/**
		 * Constructs this instance.
		 * @return New instance
		 * @throws IllegalArgumentException if the Vulkan API or application name have not been populated
		 * @throws VulkanException if the instance cannot be created
		 */
		public Instance build() {
			// Validate
			Check.notNull(lib);
			Check.notEmpty(name);

			// Init application descriptor
			final VkApplicationInfo app = new VkApplicationInfo();
			app.pApplicationName = name;
			app.applicationVersion = ver.toInteger();
			app.pEngineName = "JOVE";
			app.engineVersion = new Version(1, 0, 0).toInteger();
			app.apiVersion = VulkanLibrary.VERSION.toInteger();

			// Init instance descriptor
			final VkInstanceCreateInfo info = new VkInstanceCreateInfo();
			info.pApplicationInfo = app;

			// Populate required extensions
			info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
			info.enabledExtensionCount = extensions.size();

			// Populate required layers
			info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
			info.enabledLayerCount = layers.size();

			// Create instance
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateInstance(info, null, handle));

			// Create instance wrapper
			return new Instance(lib, handle.getValue());
		}
	}
}
