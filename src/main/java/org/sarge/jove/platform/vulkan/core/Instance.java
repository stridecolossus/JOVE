package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.sarge.jove.util.ReferenceFactory;
import org.sarge.lib.util.Check;

import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * An <i>instance</i> is the root object for a Vulkan application.
 * @author Sarge
 */
public class Instance extends AbstractTransientNativeObject {
	private final VulkanLibrary lib;
	private final ReferenceFactory factory;
	private HandlerManager manager;

	/**
	 * Constructor.
	 * @param lib			Vulkan library
	 * @param handle		Instance handle
	 */
	Instance(Handle handle, VulkanLibrary lib, ReferenceFactory factory) {
		super(handle);
		this.lib = notNull(lib);
		this.factory = notNull(factory);
	}

	/**
	 * @return Vulkan library
	 */
	VulkanLibrary library() {
		return lib;
	}

	/**
	 * @return Reference factory
	 */
	ReferenceFactory factory() {
		return factory;
	}

	/**
	 * Looks up a Vulkan function by name.
	 * @param name Function name
	 * @return Vulkan function
	 * @throws RuntimeException if the function cannot be found
	 */
	public Function function(String name) {
		// Lookup function pointer
		final Pointer ptr = lib.vkGetInstanceProcAddr(this, name);
		if(ptr == null) throw new RuntimeException("Cannot find function pointer: " + name);

		// Convert to function (first case supports unit-tests)
		if(ptr instanceof Function func) {
			return func;
		}
		else {
			return Function.getFunction(ptr);
		}
	}

	/**
	 * @return Manager for diagnostic handlers
	 */
	public synchronized HandlerManager manager() {
		if(manager == null) {
			manager = new HandlerManager(this);
		}
		return manager;
	}

	@Override
	protected void release() {
		if(manager != null) {
			manager.close();
		}
		lib.vkDestroyInstance(handle, null);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("manager", manager)
				.build();
	}

	/**
	 * Builder for an instance.
	 */
	public static class Builder {
		private static final Version VERSION = new Version(1, 0, 0);

		private String name = "Unspecified";
		private Version ver = VERSION;
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
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
			final VkApplicationInfo app = new VkApplicationInfo();
			app.pApplicationName = name;
			app.applicationVersion = ver.toInteger();
			app.pEngineName = "JOVE";
			app.engineVersion = VERSION.toInteger();
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
			final PointerByReference ref = factory.pointer();
			check(lib.vkCreateInstance(info, null, ref));

			// Create instance domain wrapper
			final Handle handle = new Handle(ref.getValue());
			return new Instance(handle, lib, factory);
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
		void vkDestroyInstance(Handle instance, Pointer allocator);

		/**
		 * Enumerates extension properties.
		 * @param filter		Layer name or <tt>null</tt> for all
		 * @param count			Number of extensions
		 * @param extensions	Extensions
		 * @return Result
		 */
		int vkEnumerateInstanceExtensionProperties(String pLayerName, IntByReference count, VkExtensionProperties extensions);

		/**
		 * Enumerates validation layer properties.
		 * @param count			Number of layers
		 * @param layers		Layers
		 * @return Result
		 */
		int vkEnumerateInstanceLayerProperties(IntByReference count, VkLayerProperties layers);

		/**
		 * Looks up an instance function.
		 * @param instance		Vulkan instance
		 * @param name			Function name
		 * @return Function pointer
		 */
		Pointer vkGetInstanceProcAddr(Instance instance, String name);
	}
}
