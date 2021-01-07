package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.apache.commons.lang3.StringUtils.removeStart;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverityFlagEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageTypeFlagEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.api.Version;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.LazySupplier;

import com.sun.jna.Callback;
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
	private final LazySupplier<Manager> manager;

	/**
	 * Constructor.
	 * @param lib			Vulkan library
	 * @param handle		Instance handle
	 */
	private Instance(VulkanLibrary lib, Pointer handle) {
		super(handle);
		this.manager = new LazySupplier<>(() -> new Manager(handle));
		this.lib = notNull(lib);
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
		final Pointer ptr = lib.vkGetInstanceProcAddr(handle, name);
		if(ptr == null) throw new RuntimeException("Cannot find function pointer: " + name);
		return Function.getFunction(ptr);
	}

	/**
	 * Creates a builder for a new message handler to be attached to this instance.
	 * @return New message handler builder
	 */
	public Handler handler() {
		return new Handler(manager.get());
	}

	@Override
	protected void release() {
		manager.get().destroy();
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

	/**
	 * The <i>manager</i> is used to attach diagnostics message handlers to this instance.
	 */
	private class Manager {
		private final Pointer dev;
		private final Collection<Pointer> handlers = new ArrayList<>();
		private final LazySupplier<Function> create = new LazySupplier<>(() -> function("vkCreateDebugUtilsMessengerEXT"));

		/**
		 * Constructor.
		 * @param dev Logical device handle
		 */
		private Manager(Pointer dev) {
			this.dev = dev;
		}

		/**
		 * Creates a message handler.
		 * @param info Handler descriptor
		 */
		private void create(VkDebugUtilsMessengerCreateInfoEXT info) {
			// Create handler
			final PointerByReference handle = lib.factory().pointer();
			final Object[] args = {dev, info, null, handle};
			check(create.get().invokeInt(args));

			// Register handler
			handlers.add(handle.getValue());
		}

		/**
		 * Destroys all attached handlers.
		 */
		private void destroy() {
			// Ignore if unused
			if(handlers.isEmpty()) {
				return;
			}

			// Lookup destroy API method
			final Function destroy = function("vkDestroyDebugUtilsMessengerEXT");

			// Release handlers
			for(Pointer handle : handlers) {
				final Object[] args = new Object[]{dev, handle, null};
				destroy.invoke(args);
			}
		}
	}

	/**
	 * A <i>message</i> is a diagnostics report generated by Vulkan.
	 */
	public record Message(VkDebugUtilsMessageSeverityFlagEXT severity, Collection<VkDebugUtilsMessageTypeFlagEXT> types, VkDebugUtilsMessengerCallbackDataEXT data) {
		/**
		 * Message handler that outputs to the console.
		 */
		public static final Consumer<Message> CONSOLE = writer(new PrintWriter(System.out));

		/**
		 * Creates a message handler that writes a message to the given output stream.
		 * @param out Output stream
		 * @return New message handler
		 */
		public static Consumer<Message> writer(PrintWriter out) {
			return out::println;
		}

		/**
		 * Helper - Converts the given severity flag to a human-readable string.
		 * @param severity Message severity
		 * @return Severity string
		 */
		public static String toString(VkDebugUtilsMessageSeverityFlagEXT severity) {
			return clean(severity.name(), "SEVERITY");
		}

		/**
		 * Helper - Converts the given message type to a human-readable string.
		 * @param type Message type
		 * @return Message type string
		 */
		public static String toString(VkDebugUtilsMessageTypeFlagEXT type) {
			return clean(type.name(), "TYPE");
		}

		/**
		 * Helper - Strips the surrounding text from the given enumeration constant name.
		 * @param name Enumeration constant name
		 * @param type Type name
		 * @return Cleaned name
		 */
		private static String clean(String name, String type) {
			final String prefix = new StringBuilder()
					.append("VK_DEBUG_UTILS_MESSAGE_")
					.append(type)
					.append("_")
					.toString();

			return removeEnd(removeStart(name, prefix), "_BIT_EXT");
		}

		/**
		 * Constructor.
		 * @param severity		Severity
		 * @param types			Message type(s)
		 * @param data			Message data
		 */
		public Message {
			Check.notEmpty(severity);
			Check.notEmpty(types);
			Check.notNull(data);
		}

		/**
		 * Constructs a string representation of this message.
		 * <p>
		 * The message text is a colon-delimited string comprised of the following elements:
		 * <ul>
		 * <li>severity</li>
		 * <li>type(s)</li>
		 * <li>message identifier</li>
		 * <li>message text</li>
		 * </ul>
		 * Example:
		 * <code>ERROR:VALIDATION-GENERAL:1234:message</code>
		 * <p>
		 * @return Message text
		 */
		@Override
		public String toString() {
			final String compoundTypes = types.stream().map(Message::toString).collect(joining("-"));
			final StringJoiner str = new StringJoiner(":");
			str.add(toString(severity));
			str.add(compoundTypes);
			if(!data.pMessage.contains(data.pMessageIdName)) {
				str.add(data.pMessageIdName);
			}
			str.add(data.pMessage);
			return str.toString();
		}
	}

	/**
	 * Builder for a message handler.
	 * <p>
	 * Example usage that attaches a handler that outputs validation errors to the error console:
	 * <pre>
	 *  instance
	 *  	.handler()
	 *  	.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
	 *  	.type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT)
	 *  	.handler(System.err::println)
	 *  	.attach();
	 */
	public static class Handler {
		/**
		 * A <i>message callback</i> is invoked by Vulkan to report errors, diagnostics, etc.
		 */
		private static class MessageCallback implements Callback {
			private final Consumer<Message> consumer;

			/**
			 * Constructor.
			 * @param manager Message handler
			 */
			private MessageCallback(Consumer<Message> consumer) {
				this.consumer = notNull(consumer);
			}

			/**
			 * Invoked on receipt of a Vulkan diagnostics message.
			 * @param severity			Severity
			 * @param type				Message type(s)
			 * @param pCallbackData		Message
			 * @param pUserData			User data
			 * @return Whether to terminate or continue
			 */
			@SuppressWarnings("unused")
			public boolean message(int severity, int type, VkDebugUtilsMessengerCallbackDataEXT pCallbackData, Pointer pUserData) {
				// Transform bit-masks to enumerations
				final VkDebugUtilsMessageSeverityFlagEXT severityEnum = IntegerEnumeration.map(VkDebugUtilsMessageSeverityFlagEXT.class, severity);
				final Collection<VkDebugUtilsMessageTypeFlagEXT> typesEnum = IntegerEnumeration.enumerate(VkDebugUtilsMessageTypeFlagEXT.class, type);

				// Create message wrapper
				final Message message = new Message(severityEnum, typesEnum, pCallbackData);

				// Delegate to handler
				consumer.accept(message);

				// Continue execution
				return false;
			}
		}

		private final Manager manager;
		private final Set<VkDebugUtilsMessageSeverityFlagEXT> severity = new HashSet<>();
		private final Set<VkDebugUtilsMessageTypeFlagEXT> types = new HashSet<>();
		private Consumer<Message> handler = Message.CONSOLE;

		/**
		 * Constructor.
		 */
		private Handler(Manager manager) {
			this.manager = manager;
		}

		/**
		 * Sets the message handler (default is {@link Message#CONSOLE}).
		 * @param handler Message handler
		 */
		public Handler handler(Consumer<Message> handler) {
			this.handler = notNull(handler);
			return this;
		}

		/**
		 * Adds a message severity to be reported by this handler.
		 * @param severity Message severity
		 */
		public Handler severity(VkDebugUtilsMessageSeverityFlagEXT severity) {
			this.severity.add(notNull(severity));
			return this;
		}

		/**
		 * Adds a message type to be reported by this handler.
		 * @param type Message type
		 */
		public Handler type(VkDebugUtilsMessageTypeFlagEXT type) {
			types.add(notNull(type));
			return this;
		}

		/**
		 * Convenience method to initialise this builder to the following default settings:
		 * <ul>
		 * <li>warnings or higher</li>
		 * <li>general or validation message types</li>
		 * </ul>
		 */
		public Handler init() {
			severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT);
			severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
			type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT);
			type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT);
			return this;
		}

		/**
		 * Constructs this message handler and attaches it to the instance.
		 * @throws IllegalArgumentException if the message severities or message types are empty
		 */
		public void attach() {
			// Validate
			if(severity.isEmpty()) throw new IllegalArgumentException("No message severities specified");
			if(types.isEmpty()) throw new IllegalArgumentException("No message types specified");

			// Create handler descriptor
			final VkDebugUtilsMessengerCreateInfoEXT info = new VkDebugUtilsMessengerCreateInfoEXT();
			info.messageSeverity = IntegerEnumeration.mask(severity);
			info.messageType = IntegerEnumeration.mask(types);
			info.pfnUserCallback = new MessageCallback(handler);
			info.pUserData = null;

			// Create handler
			manager.create(info);
		}
	}
}
