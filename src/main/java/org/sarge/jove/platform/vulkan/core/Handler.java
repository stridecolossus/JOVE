package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.joining;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.Consumer;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.util.IntegerEnumeration;
import org.sarge.jove.platform.util.IntegerEnumeration.ReverseMapping;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.lib.util.Check;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>handler</i> is a consumer for Vulkan diagnostic messages.
 * @see Instance#handler()
 * @author Sarge
 */
public class Handler extends AbstractTransientNativeObject {
	/**
	 * Debug utility extension name.
	 */
	public static final String EXTENSION = "VK_EXT_debug_utils";

	private final Instance instance;

	/**
	 * Constructor.
	 * @param handle 		Handle
	 * @param instance		Parent instance
	 */
	private Handler(Handle handle, Instance instance) {
		super(handle);
		this.instance = notNull(instance);
	}

	@Override
	protected void release() {
		final Function destroy = instance.function("vkDestroyDebugUtilsMessengerEXT");
		final Object[] args = {instance, this, null};
		invoke(destroy, Void.class, args);
	}

	private static Object invoke(Function func, Class<?> returnType, Object[] args) {
		return func.invoke(returnType, args, VulkanLibrary.options());
	}

	/**
	 * A <i>message</i> is a diagnostics report generated by Vulkan.
	 */
	public record Message(VkDebugUtilsMessageSeverity severity, Set<VkDebugUtilsMessageType> types, VkDebugUtilsMessengerCallbackData data) {
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
			final StringJoiner str = new StringJoiner(":");
			str.add(severity.name());
			str.add(compoundTypes());
			if(!data.pMessage.contains(data.pMessageIdName)) {
				str.add(data.pMessageIdName);
			}
			str.add(data.pMessage);
			return str.toString();
		}

		private String compoundTypes() {
			return types
					.stream()
					.sorted()
					.map(Enum::name)
					.collect(joining("-"));
		}
	}

	/**
	 * Message callback.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>A JNA callback <b>must</b> implement a <b>single</b> method (though this is not enforced at compile-time)</li>
	 * <li>The signature of the callback is not part of the public Vulkan API</li>
	 * <li>TODO - link to VK doc</li>
	 * </ul>
	 */
	static class MessageCallback implements Callback {
		private static final ReverseMapping<VkDebugUtilsMessageSeverity> SEVERITY = IntegerEnumeration.reverse(VkDebugUtilsMessageSeverity.class);
		private static final ReverseMapping<VkDebugUtilsMessageType> TYPE = IntegerEnumeration.reverse(VkDebugUtilsMessageType.class);

		private final Consumer<Message> consumer;

		MessageCallback(Consumer<Message> consumer) {
			this.consumer = consumer;
		}

		/**
		 * Callback handler method.
		 * @param severity			Severity
		 * @param type				Message type(s) bit-field
		 * @param pCallbackData		Data
		 * @param pUserData			Optional user data (always {@code null})
		 * @return Whether to continue execution (always {@code false})
		 */
		public boolean message(int severity, int type, VkDebugUtilsMessengerCallbackData pCallbackData, Pointer pUserData) {
			// Transform bit-masks to enumerations
			final VkDebugUtilsMessageSeverity severities = SEVERITY.map(severity);
			final Set<VkDebugUtilsMessageType> types = TYPE.enumerate(type);

			// Wrap and delegate to handler
			final Message message = new Message(severities, types, pCallbackData);
			consumer.accept(message);

			// Continue execution
			return false;
		}
	}

	/**
	 * Builder for a diagnostics handler.
	 * <p>
	 * Usage:
	 * <pre>
	 * new Builder(instance)
	 *     .severity(VkDebugUtilsMessageSeverity.ERROR)
	 *     .type(VkDebugUtilsMessageType.GENERAL)
	 *     .consumer(System.err::println)
	 *     .build();
	 * </pre>
	 * Notes:
	 * <ul>
	 * <li>if not explicitly configured the severity and types are initialised to default values</li>
	 * <li>the default message consumer dumps diagnostic reports to the error console</li>
	 * </ul>
	 */
	public static class Builder {
		private final Instance instance;
		private final Set<VkDebugUtilsMessageSeverity> severity = new HashSet<>();
		private final Set<VkDebugUtilsMessageType> types = new HashSet<>();
		private Consumer<Message> consumer = System.err::println;

		/**
		 * Constructor.
		 * @param instance Parent instance
		 */
		public Builder(Instance instance) {
			this.instance = notNull(instance);
		}

		/**
		 * Sets the message consumer (messages are dumped to the error console by default).
		 * @param consumer Message consumer
		 */
		public Builder consumer(Consumer<Message> consumer) {
			this.consumer = notNull(consumer);
			return this;
		}

		/**
		 * Adds a message severity to be reported by this handler.
		 * @param severity Message severity
		 */
		public Builder severity(VkDebugUtilsMessageSeverity severity) {
			this.severity.add(notNull(severity));
			return this;
		}

		/**
		 * Adds a message type to be reported by this handler.
		 * @param type Message type
		 */
		public Builder type(VkDebugUtilsMessageType type) {
			types.add(notNull(type));
			return this;
		}

		/**
		 * Builds and attaches this handler.
		 * @throws IllegalArgumentException if the message severities or types is empty
		 */
		public Handler build() {
			// Init default configuration if not explicitly specified
			if(severity.isEmpty()) {
				severity(VkDebugUtilsMessageSeverity.WARNING);
				severity(VkDebugUtilsMessageSeverity.ERROR);
			}
			if(types.isEmpty()) {
				type(VkDebugUtilsMessageType.GENERAL);
				type(VkDebugUtilsMessageType.VALIDATION);
			}

			// Build handler descriptor
			final var info = new VkDebugUtilsMessengerCreateInfoEXT();
			info.messageSeverity = IntegerEnumeration.reduce(severity);
			info.messageType = IntegerEnumeration.reduce(types);
			info.pfnUserCallback = new MessageCallback(consumer);
			info.pUserData = null;

			// Lookup creation function pointer
			final Function create = instance.function("vkCreateDebugUtilsMessengerEXT");

			// Register handler with instance
			final PointerByReference ref = instance.factory().pointer();
			final Object[] args = {instance, info, null, ref};
			final int result = (int) invoke(create, Integer.class, args);
			check(result);

			// Create handler
			return new Handler(new Handle(ref), instance);
		}
	}
}
