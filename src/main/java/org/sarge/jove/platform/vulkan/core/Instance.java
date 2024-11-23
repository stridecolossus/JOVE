package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireNotEmpty;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;

/**
 * The <i>instance</i> is the root object for a Vulkan application.
 * @author Sarge
 */
public class Instance extends TransientNativeObject {
	private final Vulkan vulkan;
	private final Collection<DiagnosticHandler> handlers = new ArrayList<>();

	/**
	 * Constructor.
	 * @param handle	Instance handle
	 * @param vulkan	Vulkan
	 */
	Instance(Handle handle, Vulkan vulkan) {
		super(handle);
		this.vulkan = requireNonNull(vulkan);
	}

	/**
	 * @return Vulkan platform
	 */
	public Vulkan vulkan() {
		return vulkan;
	}

	/**
	 * Looks up a function pointer in this instance.
	 * @param name Function name
	 * @return Function pointer
	 * @throws IllegalArgumentException if the function cannot be found
	 */
	public Handle function(String name) {
		final Handle handle = vulkan.library().vkGetInstanceProcAddr(this, name);
		if(handle == null) throw new IllegalArgumentException("Instance function not found: " + name);
		return handle;
	}

	/**
	 * Creates a builder for a diagnostic handler to attach to this instance.
	 * @return Diagnostic handler builder
	 */
	public DiagnosticHandler.Builder handler() {
		return new DiagnosticHandler.Builder(this) {
			@Override
			public DiagnosticHandler build() {
				final DiagnosticHandler handler = super.build();
				handlers.add(handler);
				return handler;
			}
		};
	}

	@Override
	protected void release() {
		for(DiagnosticHandler handler : handlers) {
			handler.destroy();
		}
		handlers.clear();
		vulkan.library().vkDestroyInstance(this, null);
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

		/**
		 * Sets the application name.
		 * @param name Application name
		 */
		public Builder name(String name) {
			this.name = requireNotEmpty(name);
			return this;
		}

		/**
		 * Sets the application version (default is {@link Version#DEFAULT}).
		 * @param version Application version
		 */
		public Builder version(Version version) {
			this.ver = requireNonNull(version);
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
			this.api = requireNonNull(api);
			return this;
		}

		/**
		 * Registers a required extension.
		 * @param extension Extension name
		 */
		public Builder extension(String extension) {
			requireNotEmpty(extension);
			extensions.add(extension);
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
			requireNonNull(layer);
			layers.add(layer.name());
			return this;
		}

		/**
		 * Constructs this instance.
		 * @param lib Vulkan
		 * @return New instance
		 */
		public Instance build(Vulkan vulkan) {
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
			info.ppEnabledExtensionNames = extensions.toArray(String[]::new);		// TODO - helper class/method or collection?
			info.enabledExtensionCount = extensions.size();

			// Populate required layers
			info.ppEnabledLayerNames = layers.toArray(String[]::new);				// TODO - helper class/method or collection?
			info.enabledLayerCount = layers.size();

			// Create instance
			final PointerReference ref = vulkan.factory().pointer();
			vulkan.library().vkCreateInstance(info, null, ref);

			// Create instance domain wrapper
			return new Instance(ref.handle(), vulkan);
		}
	}

	/**
	 * Vulkan API for instance management.
	 */
	interface Library {
		/**
		 * Creates the vulkan instance.
		 * @param info				Instance descriptor
		 * @param pAllocator		Allocator
		 * @param pInstance			Returned instance handle
		 * @return Result
		 */
		int vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, PointerReference pInstance);

		/**
		 * Destroys the vulkan instance.
		 * @param instance			Instance handle
		 * @param pAllocator		Allocator
		 */
		void vkDestroyInstance(Instance instance, Handle pAllocator);

		/**
		 * Enumerates extension properties.
		 * @param pLayerName		Optional layer name
		 * @param pPropertyCount	Number of extensions
		 * @param pProperties		Extensions
		 * @return Result
		 */
		int vkEnumerateInstanceExtensionProperties(String pLayerName, IntegerReference pPropertyCount, @Returned VkExtensionProperties[] pProperties);

		/**
		 * Enumerates validation layer properties.
		 * @param pPropertyCount	Number of layers
		 * @param pProperties		Layers
		 * @return Result
		 */
		int vkEnumerateInstanceLayerProperties(IntegerReference pPropertyCount, @Returned VkLayerProperties[] pProperties);

		/**
		 * Looks up a function pointer of this instance.
		 * @param instance		Vulkan instance
		 * @param name			Function name
		 * @return Function pointer
		 */
		Handle vkGetInstanceProcAddr(Instance instance, String pName);
	}
}
