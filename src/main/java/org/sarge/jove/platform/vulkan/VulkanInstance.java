package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.joining;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.StringJoiner;

import org.sarge.jove.platform.Handle;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.Resource;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.Vulkan.ReferenceFactory;
import org.sarge.jove.platform.vulkan.VulkanLibrary.Version;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.Check;

import com.sun.jna.Callback;
import com.sun.jna.Function;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>vulkan instance</i> is the root Vulkan object.
 * <ul>
 * <li>physical device(s) associated with this instance are retrieved using {@link #devices()}</li>
 * <li>the <i>global</i> supported extensions and layers can be queried via {@link #supported(VulkanLibrary)}</li>
 * <li>debug message handlers can be attached to the instance using the {@link #handlerFactory()}</li>
 * </ul>
 * @author Sarge
 */
public class VulkanInstance extends VulkanHandle {
	private final Vulkan vulkan;

	private MessageHandlerFactory handlerFactory;

	/**
	 * Constructor.
	 * @param instance 		Instance handle
	 * @param lib			Vulkan library
	 * @param references	Reference factory
	 */
	VulkanInstance(VulkanHandle handle, Vulkan vulkan) {
		super(handle);
		this.vulkan = notNull(vulkan);
	}

	/**
	 * Retrieves the physical device handles for this instance
	 * @return Physical devices
	 */
	public Collection<Pointer> devices() {
		final VulkanFunction<Pointer[]> func = (count, array) -> vulkan.library().vkEnumeratePhysicalDevices(super.handle(), count, array);
		final ReferenceFactory factory = vulkan.factory();
		final Pointer[] devices = VulkanFunction.array(func, factory::pointers);
		return Arrays.asList(devices);
	}

	/**
	 * @return Message handler factory for this instance
	 * @throws ServiceException if the debug extension is not active
	 */
	public synchronized MessageHandlerFactory handlerFactory() {
		if(handlerFactory == null) {
			handlerFactory = new MessageHandlerFactory();
		}
		return handlerFactory;
	}

	@Override
	protected void cleanup() {
		if(handlerFactory != null) {
			handlerFactory.destroyHandlers();
		}
	}

	/**
	 * Builder for a Vulkan instance.
	 */
	public static class Builder extends Feature.AbstractBuilder<Builder> {
		private final Vulkan vulkan;

		private String name;
		private Version ver = new Version(1, 0, 0);

		/**
		 * Constructor.
		 * @param vulkan Vulkan
		 */
		public Builder(Vulkan vulkan) {
			super(vulkan.supported());
			this.vulkan = notNull(vulkan);
		}

		/**
		 * Sets the application name.
		 * @param name Application name
		 */
		public Builder name(String name) {
			this.name = name;
			return this;
		}

		/**
		 * Sets the application version.
		 * @param ver Application version
		 */
		public Builder version(Version ver) {
			this.ver = ver;
			return this;
		}

		/**
		 * Constructs this instance.
		 * @return New instance
		 * @throws ServiceException if the instance cannot be created
		 * @throws ServiceException if the required extensions or layers are not supported
		 * @see VulkanInstance#supported(VulkanLibrary)
		 */
		public VulkanInstance build() {
			// Validate required extensions and layers
			validate();

			// Init application info
			final VkApplicationInfo app = new VkApplicationInfo();
			app.pApplicationName = name;
			app.applicationVersion = ver.toInteger();

			// Init instance descriptor
			final VkInstanceCreateInfo info = new VkInstanceCreateInfo();
			info.pApplicationInfo = app;

			// Populate required extensions
			final String[] extensions = super.extensions();
			info.ppEnabledExtensionNames = new StringArray(extensions);
			info.enabledExtensionCount = extensions.length;

			// Populate required layers
			final String[] layers = super.layers();
			info.ppEnabledLayerNames = new StringArray(layers);
			info.enabledLayerCount = layers.length;

			// Create instance
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference instance = vulkan.factory().reference();
			check(lib.vkCreateInstance(info, null, instance));

			// Create instance wrapper
			final Pointer handle = instance.getValue();
			final Destructor destructor = () -> lib.vkDestroyInstance(handle, null);
			return new VulkanInstance(new VulkanHandle(handle, destructor), vulkan);
		}
	}

	/**
	 * A <i>message callback</i> is invoked by Vulkan to report errors, diagnostics, etc.
	 * @see MessageHandlerFactory#create(MessageCallback, Set, Set, Pointer)
	 */
	public static interface MessageCallback extends Callback {
		/**
		 * Invoked on receipt of a message.
		 * @param severity			Severity
		 * @param type				Message type(s)
		 * @param pCallbackData		Message
		 * @param pUserData			User data
		 * @return Whether to terminate or continue
		 */
		boolean message(int severity, int type, VkDebugUtilsMessengerCallbackDataEXT pCallbackData, Pointer pUserData);

		/**
		 * Default implementation that applies default formatting and dumps to the console.
		 */
		MessageCallback CONSOLE = (severity, types, pCallbackData, pUserData) -> {
			final String message = new StringJoiner(":")
				.add(toString(IntegerEnumeration.map(VkDebugUtilsMessageSeverityFlagEXT.class, severity)))
				.add(toString(types))
				.add(pCallbackData.pMessageIdName)
				.add(pCallbackData.pMessage)
				.toString();
			// TODO - how to create adapter? (...) -> string -> writer
			System.out.println(message);
			return false;
		};

		/**
		 * Helper - Converts the given severity to a human-readable token.
		 * @param severity Severity
		 * @return Severity token
		 */
		static String toString(VkDebugUtilsMessageSeverityFlagEXT severity) {
			switch(severity) {
			case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT:			return "ERROR";
			case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT:		return "WARN";
			case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT:			return "INFO";
			case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT:		return "VERBOSE";
			default:													return String.valueOf(severity.value());
			}
		}

		/**
		 * Helper - Converts the given message type to a human readable token.
		 * @param type Message type
		 * @return Message type token
		 */
		static String toString(VkDebugUtilsMessageTypeFlagEXT type) {
			switch(type) {
			case VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT:		return "VALIDATION";
			case VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT:			return "GENERAL";
			case VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT:		return "PERFORMANCE";
			default:													return String.valueOf(type.value());
			}
		}

		/**
		 * Helper - Converts the given message type(s) mask to a human-readable string.
		 * @param types Message type(s)
		 * @return String
		 */
		static String toString(int types) {
			return IntegerEnumeration.enumerate(VkDebugUtilsMessageTypeFlagEXT.class, types).stream().map(MessageCallback::toString).collect(joining("-"));
		}
	}

	/**
	 * A <i>message handler factory</i> is used to attach a debug message handler to a Vulkan instance.
	 * <p>
	 * Example:
	 * <pre>
	 * // Get handler factory from instance
	 * final VulkanInstance instance = ...
	 * final MessageHandlerFactory factory = instance.factory();
	 *
	 * // Create callback
	 * final MessageCallback callback = (severity, type, pCallbackData, pUserData) -> {
	 * 	...
	 * };
	 *
	 * // Create handler
	 * final Handle handle = factory.builder()
	 * 	.callback(callback)
	 * 	.severity(VkDebugUtilsMessageSeverityFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT)
	 * 	.type(VkDebugUtilsMessageTypeFlagBitsEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT)
	 *	.build();
	 * </pre>
	 * <p>
	 * The {@link MessageCallback} interface provides several convenience methods for manipulating message severity and type enumerations.
	 * <p>
	 * @author Sarge
	 * @see <a href="chrome-extension://oemmndcbldboiebfnladdacbdfmadadm/https://www.lunarg.com/wp-content/uploads/2018/05/Vulkan-Debug-Utils_05_18_v1.pdf">Vulkan-Debug-Utils_05_18_v1.pdf</a>
	 */
	public class MessageHandlerFactory {
		private final Resource.Tracker<Handle> tracker = new Resource.Tracker<>();

		private final Function create;
		private final Function destroy;

		/**
		 * Constructor.
		 * @throws ServiceException if the debug extension is not active
		 */
		private MessageHandlerFactory() {
			this.create = function("vkCreateDebugUtilsMessengerEXT");
			this.destroy = function("vkDestroyDebugUtilsMessengerEXT");
		}

		/**
		 * Test constructor.
		 * @param create		Create method
		 * @param destroy		Destroy method
		 */
		protected MessageHandlerFactory(Function create, Function destroy) {
			this.create = notNull(create);
			this.destroy = notNull(destroy);
		}

		/**
		 * @return Create messenger function pointer
		 */
		private Function function(String name) {
			final VulkanLibrary lib = Vulkan.instance().library();
			final Pointer ptr = lib.vkGetInstanceProcAddr(VulkanInstance.this.handle(), name);
			if(ptr == null) throw new ServiceException("Cannot find debug function pointer: " + name);
			return Function.getFunction(ptr);
		}

		/**
		 * Creates a new message handler.
		 * @param callback		Debug callback
		 * @param severity		Severity filter
		 * @param type			Message type filter
		 * @param user			Optional user data
		 * @return Message handle opaque handle
		 * @throws ServiceException if the handler cannot be created
		 */
		public Handle create(MessageCallback callback, Set<VkDebugUtilsMessageSeverityFlagEXT> severity, Set<VkDebugUtilsMessageTypeFlagEXT> type, Pointer user) {
			Check.notEmpty(severity);
			Check.notEmpty(type);

			// Build handler descriptor
			final VkDebugUtilsMessengerCreateInfoEXT info = new VkDebugUtilsMessengerCreateInfoEXT();
			info.messageSeverity = IntegerEnumeration.mask(severity);
			info.messageType = IntegerEnumeration.mask(type);
			info.pfnUserCallback = notNull(callback);
			info.pUserData = user;

			// Create handler
			final PointerByReference ref = Vulkan.instance().factory().reference();
			check(create.invokeInt(new Object[]{VulkanInstance.this.handle(), info, null, ref}));

			// Create handler wrapper
			final Handle handler = new Handle(ref.getValue()) {
				@Override
				public synchronized void destroy() {
					tracker.remove(this);
					destroy.invoke(new Object[]{VulkanInstance.this.handle(), super.handle(), null});
					super.destroy();
				}
			};
			tracker.add(handler);
			return handler;
		}

		/**
		 * Destroys <b>all</b> message handlers created by this factory.
		 */
		public void destroyHandlers() {
			tracker.destroy();
		}

		/**
		 * @return New builder for a message handler
		 */
		public Builder builder() {
			return new Builder();
		}

		/**
		 * Builder for a message handler.
		 */
		public class Builder {
			private MessageCallback callback = MessageCallback.CONSOLE;
			private final Set<VkDebugUtilsMessageSeverityFlagEXT> severity = new StrictSet<>();
			private final Set<VkDebugUtilsMessageTypeFlagEXT> types = new StrictSet<>();
			private Pointer user;

			private Builder() {
			}

			/**
			 * Sets the callback function.
			 * @param callback Callback function
			 */
			public Builder callback(MessageCallback callback) {
				this.callback = callback;
				return this;
			}

			/**
			 * Convenience method to initialise the configuration of this handler to the following defaults:
			 * <ul>
			 * <li>Error and warning severity</li>
			 * <li>General and validation message types</li>
			 * </ul>
			 */
			public Builder init() {
				severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT);
				severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT);
				type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT);
				type(VkDebugUtilsMessageTypeFlagEXT.VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT);
				return this;
			}

			/**
			 * Adds a message severity to receive.
			 * @param severity Message severity
			 */
			public Builder severity(VkDebugUtilsMessageSeverityFlagEXT severity) {
				this.severity.add(severity);
				return this;
			}

			/**
			 * Adds a message type to receive.
			 * @param type Message type
			 */
			public Builder type(VkDebugUtilsMessageTypeFlagEXT type) {
				types.add(type);
				return this;
			}

			/**
			 * Sets the user data for this handler.
			 * @param user User data
			 */
			public Builder user(Pointer user) {
				this.user = user;
				return this;
			}

			/**
			 * Constructs this handler.
			 * @return New message handler
			 */
			public Handle build() {
				return create(callback, severity, types, user);
			}
		}
	}
}
