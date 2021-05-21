package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.LazySupplier;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>instance</i> is the root object for a Vulkan application.
 * @author Sarge
 */
public class Instance extends AbstractTransientNativeObject {
	private final VulkanLibrary lib;
	private final Pointer ptr;
	private final Supplier<Function> create = new LazySupplier<>(() -> function("vkCreateDebugUtilsMessengerEXT"));
	private final Collection<Pointer> handlers = new ArrayList<>();

	/**
	 * Constructor.
	 * @param lib			Vulkan library
	 * @param handle		Instance handle
	 */
	private Instance(VulkanLibrary lib, Pointer handle) {
		super(new Handle(handle));
		this.lib = notNull(lib);
		this.ptr = notNull(handle);
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
	 * @throws RuntimeException if the function cannot be found
	 */
	public Function function(String name) {
		// Lookup function pointer
		final Pointer ptr = lib.vkGetInstanceProcAddr(handle, name);
		if(ptr == null) throw new RuntimeException("Cannot find function pointer: " + name);

		// Convert to function (first case is a test helper)
		if(ptr instanceof Function func) {
			return func;
		}
		else {
			return Function.getFunction(ptr);
		}
	}

	/**
	 * Attaches a diagnostics message handler to this instance.
	 * @param handler Message handler descriptor
	 * @throws RuntimeException if the diagnostics extension is not present
	 */
	public void attach(VkDebugUtilsMessengerCreateInfoEXT handler) {
		final PointerByReference handle = lib.factory().pointer();
		final Object[] args = {ptr, handler, null, handle};
		check(create.get().invokeInt(args));
		handlers.add(handle.getValue());
	}

	@Override
	protected void release() {
		if(!handlers.isEmpty()) {
			final Function destroy = function("vkDestroyDebugUtilsMessengerEXT");
			for(Pointer handle : handlers) {
				final Object[] args = {ptr, handle, null};
				destroy.invoke(args);
			}
		}

		lib.vkDestroyInstance(handle, null);
	}

	/**
	 * Builder for an instance.
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
