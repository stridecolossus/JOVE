package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireNotEmpty;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

/**
 * The <i>instance</i> is the root object for a Vulkan application.
 * @author Sarge
 */
public class Instance extends TransientNativeObject {
	private final Library library;

	/**
	 * Constructor.
	 * @param handle		Instance handle
	 * @param library		Instance library
	 */
	Instance(Handle handle, Instance.Library library) {
		super(handle);
		this.library = requireNonNull(library);
	}

	/**
	 * Looks up a function pointer in this instance.
	 * @param name Function name
	 * @return Function pointer
	 */
	public Optional<Handle> function(String name) {
		final Handle function = library.vkGetInstanceProcAddr(this, name);
		return Optional.ofNullable(function);
	}

	@Override
	protected void release() {
		library.vkDestroyInstance(this, null);
	}

	/**
	 * Enumerates the extensions supported by this Vulkan implementation.
	 * @param library		Instance library
	 * @param layer			Optional layer name
	 * @return Supported extensions
	 */
	public static VkExtensionProperties[] extensions(Library library, String layer) {
		final VulkanFunction<VkExtensionProperties[]> extensions = (count, array) -> library.vkEnumerateInstanceExtensionProperties(layer, count, array);
		return VulkanFunction.invoke(extensions, VkExtensionProperties[]::new);
	}

	/**
	 * Enumerates the layers supported by this Vulkan implementation.
	 * @param library Instance library
	 * @return Supported layers
	 */
	public static VkLayerProperties[] layers(Library library) {
		final VulkanFunction<VkLayerProperties[]> layers = (count, array) -> library.vkEnumerateInstanceLayerProperties(count, array);
		return VulkanFunction.invoke(layers, VkLayerProperties[]::new);
	}

	/**
	 * Builder for an instance.
	 */
	public static class Builder {
		private String name = "Unspecified";
		private Version version = Version.DEFAULT;
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private Version api = Vulkan.VERSION;

		/**
		 * Sets the application name.
		 * @param name Application name
		 */
		public Builder name(String name) {
			this.name = requireNotEmpty(name);
			return this;
		}

		/**
		 * Sets the application version.
		 * @param version Application version
		 * @see Version#DEFAULT
		 */
		public Builder version(Version version) {
			this.version = requireNonNull(version);
			return this;
		}

		/**
		 * Sets the required version of the Vulkan API.
		 * @param api Required API version
		 * @throws IllegalArgumentException if {@link #api} is not supported by this JOVE implementation
		 * @see Vulkan#VERSION
		 */
		public Builder api(Version api) {
			if(api.compareTo(Vulkan.VERSION) > 0) {
				throw new IllegalArgumentException("Required API not supported by this implementation: required=%s supported=%s".formatted(api, Vulkan.VERSION));
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
		 * Helper - Registers a group of extensions.
		 * @param extensions Extension names
		 */
		public Builder extensions(Iterable<String> extensions) {
			for(String ext : extensions) {
				extension(ext);
			}
			return this;
		}

		/**
		 * Registers a required validation layer.
		 * @param layer Validation layer descriptor
		 */
		public Builder layer(String layer) {
			requireNotEmpty(layer);
			layers.add(layer);
			return this;
		}

		/**
		 * Constructs this instance.
		 * @param lib Vulkan
		 * @return New instance
		 */
		public Instance build(Library lib) {
			final VkInstanceCreateInfo info = create();
			final var ref = new Pointer();
			lib.vkCreateInstance(info, null, ref);
			return new Instance(ref.get(), lib);
		}

		/**
		 * @return Application descriptor
		 */
		private VkApplicationInfo application() {
			final var app = new VkApplicationInfo();
			app.pApplicationName = name;
			app.applicationVersion = version.toInteger();
			app.pEngineName = "JOVE";
			app.engineVersion = new Version(1, 0, 0).toInteger();
			app.apiVersion = api.toInteger();
			return app;
		}

		/**
		 * @return Instance descriptor
		 */
		private VkInstanceCreateInfo create() {
			// Init instance descriptor
			final var info = new VkInstanceCreateInfo();
			info.pApplicationInfo = application();

			// Populate required extensions
			info.ppEnabledExtensionNames = extensions.toArray(String[]::new);
			info.enabledExtensionCount = extensions.size();

			// Populate required layers
			info.ppEnabledLayerNames = layers.toArray(String[]::new);
			info.enabledLayerCount = layers.size();

			return info;
		}
	}

	/**
	 * Vulkan API for instance management.
	 */
	public interface Library {
		/**
		 * Creates the vulkan instance.
		 * @param info				Instance descriptor
		 * @param pAllocator		Allocator
		 * @param pInstance			Returned instance handle
		 * @return Result
		 */
		VkResult vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, Pointer pInstance);

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
		VkResult vkEnumerateInstanceExtensionProperties(String pLayerName, IntegerReference pPropertyCount, @Returned VkExtensionProperties[] pProperties);

		/**
		 * Enumerates validation layer properties.
		 * @param pPropertyCount	Number of layers
		 * @param pProperties		Layers
		 * @return Result
		 */
		VkResult vkEnumerateInstanceLayerProperties(IntegerReference pPropertyCount, @Returned VkLayerProperties[] pProperties);

		/**
		 * Looks up a function pointer of this instance.
		 * @param instance		Vulkan instance
		 * @param pName			Function name
		 * @return Function pointer
		 */
		Handle vkGetInstanceProcAddr(Instance instance, String pName);
	}
}
