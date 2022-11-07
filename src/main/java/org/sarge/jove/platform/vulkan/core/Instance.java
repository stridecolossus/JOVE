package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.sarge.jove.util.ReferenceFactory;
import org.sarge.lib.util.Check;

import com.sun.jna.*;
import com.sun.jna.ptr.*;

/**
 * An <i>instance</i> is the root object for a Vulkan application.
 * @author Sarge
 */
public class Instance extends AbstractTransientNativeObject {
	private final VulkanLibrary lib;
	private final ReferenceFactory factory;
	private final Map<String, Function> functions = new HashMap<>();
	private final Collection<Handler> handlers = new ArrayList<>();

	/**
	 * Constructor.
	 * @param handle		Instance handle
	 * @param lib			Vulkan library
	 * @param factory		Reference factory
	 */
	Instance(Handle handle, VulkanLibrary lib, ReferenceFactory factory) {
		super(handle);
		this.lib = notNull(lib);
		this.factory = notNull(factory);
	}

	/**
	 * @return Vulkan library
	 */
	public VulkanLibrary library() {
		return lib;
	}

	/**
	 * @return Reference factory
	 */
	public ReferenceFactory factory() {
		return factory;
	}

	/**
	 * Looks up a Vulkan function by name.
	 * @param name Function name
	 * @return Vulkan function
	 * @throws RuntimeException if the function cannot be found
	 */
	public Function function(String name) {
		return functions.computeIfAbsent(name, this::lookup);
	}

	private Function lookup(String name) {
		final Pointer ptr = lib.vkGetInstanceProcAddr(this, name);
		if(ptr == null) throw new RuntimeException("Cannot find function pointer: " + name);
		return Function.getFunction(ptr);
	}

	/**
	 * Attaches a diagnostic handler to this instance.
	 */
	void attach(Handler handler) {
		handlers.add(notNull(handler));
	}

	@Override
	protected void release() {
		// Release diagnostic handlers
		for(Handler handler : handlers) {
			if(!handler.isDestroyed()) {
				handler.destroy();
			}
		}
		handlers.clear();

		// Release this instance
		lib.vkDestroyInstance(this, null);
	}

	/**
	 * Builder for an instance.
	 */
	public static class Builder {
		private static final Version VERSION = Version.DEFAULT;

		private String name = "Unspecified";
		private Version ver = Version.DEFAULT;
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private Version api = VulkanLibrary.VERSION;
		private ReferenceFactory factory = ReferenceFactory.DEFAULT;

		/**
		 * Sets the application name.
		 * @param name Application name
		 */
		public Builder name(String name) {
			this.name = notEmpty(name);
			return this;
		}

		/**
		 * Sets the application version (default is {@link Version#DEFAULT}).
		 * @param ver Application version
		 */
		public Builder version(Version ver) {
			this.ver = notNull(ver);
			return this;
		}

		/**
		 * Sets the required version of the Vulkan API (default is {@link VulkanLibrary#VERSION}).
		 * @param api Required API version
		 * @throws IllegalStateException if {@link #api} is not supported by this JOVE implementation
		 */
		public Builder api(Version api) {
			if(api.compareTo(VulkanLibrary.VERSION) > 0) {
				throw new IllegalStateException("Required API not supported by this implementation: required=%s supported=%s".formatted(api, VulkanLibrary.VERSION));
			}
			this.api = notNull(api);
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
		 * Helper - Registers an array of extensions.
		 * @param extensions Extension names
		 */
		public Builder extensions(String[] extensions) {
			for(String ext : extensions) {
				extension(ext);
			}
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
		 * Sets the reference factory used by this instance.
		 * @param factory Reference factory
		 * @see ReferenceFactory#DEFAULT
		 */
		public Builder factory(ReferenceFactory factory) {
			this.factory = notNull(factory);
			return this;
		}

		/**
		 * Constructs this instance.
		 * @param lib Vulkan
		 * @return New instance
		 */
		public Instance build(VulkanLibrary lib) {
			// Init application descriptor
			final var app = new VkApplicationInfo();
			app.pApplicationName = name;
			app.applicationVersion = ver.toInteger();
			app.pEngineName = "JOVE";
			app.engineVersion = VERSION.toInteger();
			app.apiVersion = api.toInteger();

			// Init instance descriptor
			final var info = new VkInstanceCreateInfo();
			info.pApplicationInfo = app;

			// Populate required extensions
			info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
			info.enabledExtensionCount = extensions.size();

			// Populate required layers
			info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
			info.enabledLayerCount = layers.size();

			// Create instance
			final PointerByReference ref = factory.pointer();
			check(lib.vkCreateInstance(info, null, ref));

			// Create instance domain wrapper
			return new Instance(new Handle(ref), lib, factory);
		}
	}

	/**
	 * Vulkan API for instance management.
	 */
	interface Library {
		/**
		 * Creates a vulkan instance.
		 * @param info			Instance descriptor
		 * @param allocator		Allocator
		 * @param instance		Returned instance
		 * @return Result
		 */
		int vkCreateInstance(VkInstanceCreateInfo info, Pointer allocator, PointerByReference instance);

		/**
		 * Destroys the vulkan instance.
		 * @param instance		Instance handle
		 * @param allocator		Allocator
		 */
		void vkDestroyInstance(Instance instance, Pointer allocator);

		/**
		 * Enumerates extension properties.
		 * @param pLayerName	Layer name or {@code null} for extensions provided by the Vulkan implementation
		 * @param count			Number of extensions
		 * @param extensions	Extensions (pointer-to-array)
		 * @return Result
		 */
		int vkEnumerateInstanceExtensionProperties(String pLayerName, IntByReference count, VkExtensionProperties extensions);

		/**
		 * Enumerates validation layer properties.
		 * @param count			Number of layers
		 * @param layers		Layers (pointer-to-array)
		 * @return Result
		 */
		int vkEnumerateInstanceLayerProperties(IntByReference count, VkLayerProperties layers);

		/**
		 * Looks up a function pointer for the given instance.
		 * @param instance		Vulkan instance
		 * @param name			Function name
		 * @return Function pointer
		 */
		Pointer vkGetInstanceProcAddr(Instance instance, String name);
	}
}
