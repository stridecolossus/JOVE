package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.joining;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkApplicationInfo;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverity;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageType;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCallbackData;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.platform.vulkan.VkInstanceCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.common.Version;
import org.sarge.jove.platform.vulkan.util.VulkanException;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.LazySupplier;

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
	private final Supplier<Function> create = new LazySupplier<>(() -> function("vkCreateDebugUtilsMessengerEXT"));
	private final Collection<Pointer> handlers = new ArrayList<>();

	/**
	 * Constructor.
	 * @param lib			Vulkan library
	 * @param handle		Instance handle
	 */
	Instance(VulkanLibrary lib, Handle handle) {
		super(handle);
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
		// Lookup function pointer
		final Pointer ptr = lib.vkGetInstanceProcAddr(handle, name);
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
	 * @return New message handler
	 */
	public Handler handler() {
		return new Handler();
	}

	@Override
	protected void release() {
		if(!handlers.isEmpty()) {
			final Function destroy = function("vkDestroyDebugUtilsMessengerEXT");
			for(Pointer p : handlers) {
				final Object[] args = {handle.toPointer(), p, null};
				destroy.invoke(args);
			}
		}

		lib.vkDestroyInstance(handle, null);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("handlers", handlers.size()).build();
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
		 * @throws VulkanException if the instance cannot be created
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
			final PointerByReference ref = lib.factory().pointer();
			check(lib.vkCreateInstance(info, null, ref));

			// Create instance wrapper
			final Handle handle = new Handle(ref.getValue());
			return new Instance(lib, handle);
		}
	}

	/**
	 * A <i>message</i> is a diagnostics report generated by Vulkan.
	 */
	public record Message(VkDebugUtilsMessageSeverity severity, Collection<VkDebugUtilsMessageType> types, VkDebugUtilsMessengerCallbackData data) {
		/**
		 * Constructor.
		 * @param severity		Severity
		 * @param types			Message type(s)
		 * @param data			Message data
		 */
		public Message {
			Check.notNull(severity);
			Check.notEmpty(types);
			Check.notNull(data);
		}

		/**
		 * Constructs a string representation of this message.
		 * <p>
		 * The message text is a colon-delimited string comprised of the following elements:
		 * <ul>
		 * <li>severity</li>
		 * <li>type(s) separated by the hyphen character</li>
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
			final String compoundTypes = types.stream().map(Enum::name).collect(joining("-"));
			final StringJoiner str = new StringJoiner(":");
			str.add(severity.name());
			str.add(compoundTypes);
			if(!data.pMessage.contains(data.pMessageIdName)) {
				str.add(data.pMessageIdName);
			}
			str.add(data.pMessage);
			return str.toString();
		}
	}

	/**
	 * Message callback.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>a JNA callback <b>must</b> implement a <b>single</b> method (though this is not enforced at compile-time)</li>
	 * <li>the signature of the {@link #message(int, int, VkDebugUtilsMessengerCallbackData, Pointer)} callback is not part of the public Vulkan API</li>
	 * <li>TODO - link to VK doc</li>
	 * </ul>
	 */
	static class MessageCallback implements Callback {
		private final Consumer<Message> consumer;

		MessageCallback(Consumer<Message> consumer) {
			this.consumer = consumer;
		}

		/**
		 * Callback handler method.
		 * @param severity			Severity
		 * @param type				Message type(s) mask
		 * @param pCallbackData		Data
		 * @param pUserData			Optional user data (generally redundant for an OO approach)
		 * @return Whether to continue execution (always {@code false})
		 */
		public boolean message(int severity, int type, VkDebugUtilsMessengerCallbackData pCallbackData, Pointer pUserData) {
			// Transform bit-masks to enumerations
			final VkDebugUtilsMessageSeverity severityEnum = IntegerEnumeration.map(VkDebugUtilsMessageSeverity.class, severity);
			final Collection<VkDebugUtilsMessageType> typesEnum = IntegerEnumeration.enumerate(VkDebugUtilsMessageType.class, type);

			// Create message wrapper
			final Message message = new Message(severityEnum, typesEnum, pCallbackData);

			// Delegate to handler
			consumer.accept(message);

			// Continue execution
			return false;
		}
	}

	/**
	 * A <i>handler</i> is a builder for a diagnostics message handler attached to this instance.
	 * <p>
	 * Usage:
	 * <pre>
	 *  Instance instance = ...
	 *  Consumer&lt;Message&gt; consumer = ...
	 *
	 *  // Attach handler
	 *  new Handler()
	 *  	.severity(VkDebugUtilsMessageSeverity.WARNING)
	 *  	.severity(VkDebugUtilsMessageSeverity.ERROR)
	 *  	.type(VkDebugUtilsMessageType.GENERAL)
	 *  	.type(VkDebugUtilsMessageType.VALIDATION)
	 *  	.consumer(consumer)
	 *  	.attach(instance);
	 *
	 *  // Alternatively
	 *  new Handler().init().attach();
	 * </pre>
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>It is assumed that handlers are attached to the instance for the duration of its lifetime</li>
	 * <li>Handlers are released when the instance itself is destroyed</li>
	 * <li>In general it is expected that a single handler will be sufficient for most or all application requirements, however the API does support multiple handlers which is followed here</li>
	 * <li>Diagnostics messages can contain an arbitrary pointer to a <i>user data</i> object but this is assumed to be redundant for an OO implementation and is always {@code null}</li>
	 * </ul>
	 */
	public class Handler {
		private final Set<VkDebugUtilsMessageSeverity> severity = new HashSet<>();
		private final Set<VkDebugUtilsMessageType> types = new HashSet<>();
		private Consumer<Message> consumer = System.err::println;

		/**
		 * Sets the message consumer (dumps messages to the error console by default).
		 * @param consumer Message consumer
		 */
		public Handler consumer(Consumer<Message> consumer) {
			this.consumer = notNull(consumer);
			return this;
		}

		/**
		 * Adds a message severity to be reported by this handler.
		 * @param severity Message severity
		 */
		public Handler severity(VkDebugUtilsMessageSeverity severity) {
			this.severity.add(notNull(severity));
			return this;
		}

		/**
		 * Adds a message type to be reported by this handler.
		 * @param type Message type
		 */
		public Handler type(VkDebugUtilsMessageType type) {
			types.add(notNull(type));
			return this;
		}

		/**
		 * Convenience method - Initialises this builder to the following defaults:
		 * <ul>
		 * <li>warnings and higher</li>
		 * <li>general or validation message types</li>
		 * </ul>
		 */
		public Handler init() {
			severity(VkDebugUtilsMessageSeverity.WARNING);
			severity(VkDebugUtilsMessageSeverity.ERROR);
			type(VkDebugUtilsMessageType.GENERAL);
			type(VkDebugUtilsMessageType.VALIDATION);
			return this;
		}

		/**
		 * Attaches this handler to the instance.
		 * @throws IllegalArgumentException if the message severities or types is empty
		 */
		public void attach() {
			// Validate
			if(severity.isEmpty()) throw new IllegalArgumentException("No message severities specified");
			if(types.isEmpty()) throw new IllegalArgumentException("No message types specified");

			// Create callback
			final MessageCallback callback = new MessageCallback(consumer);

			// Build handler descriptor
			final var info = new VkDebugUtilsMessengerCreateInfoEXT();
			info.messageSeverity = IntegerEnumeration.mask(severity);
			info.messageType = IntegerEnumeration.mask(types);
			info.pfnUserCallback = callback;
			info.pUserData = null;

			// Attach to instance
			final PointerByReference ref = lib.factory().pointer();
			final Object[] args = {handle.toPointer(), info, null, ref};
			check(create.get().invokeInt(args));
			handlers.add(ref.getValue());
		}
	}
}
