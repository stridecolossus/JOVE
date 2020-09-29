package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.joining;
import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.TreeSet;

import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageSeverityFlagEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessageTypeFlagEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCallbackDataEXT;
import org.sarge.jove.platform.vulkan.VkDebugUtilsMessengerCreateInfoEXT;
import org.sarge.jove.util.Check;

import com.sun.jna.Callback;
import com.sun.jna.Pointer;

/**
 * A <i>message handler</i> specifies how diagnostic and error messages reported by Vulkan are handled.
 * @see Instance#add(MessageHandler)
 * @author Sarge
 */
public class MessageHandler {
	/**
	 * A message handler <i>callback</i> is invoked by Vulkan to report errors, diagnostics, etc.
	 */
	public interface MessageCallback extends Callback {
		/**
		 * Invoked on receipt of a debug message.
		 * @param severity			Severity
		 * @param type				Message type(s)
		 * @param pCallbackData		Message
		 * @param pUserData			User data
		 * @return Whether to terminate or continue
		 */
		boolean message(int severity, int type, VkDebugUtilsMessengerCallbackDataEXT pCallbackData, Pointer pUserData);
	}

	/**
	 * Adapter for a message callback that maps the severity and type fields to the corresponding enumerations.
	 * @see #message(VkDebugUtilsMessageSeverityFlagEXT, Set, VkDebugUtilsMessengerCallbackDataEXT)
	 */
	public static abstract class AbstractMessageCallback implements MessageCallback {
		@Override
		public final boolean message(int severity, int type, VkDebugUtilsMessengerCallbackDataEXT pCallbackData, Pointer pUserData) {
			final var messageSeverity = IntegerEnumeration.map(VkDebugUtilsMessageSeverityFlagEXT.class, severity);
			final var messageTypes = IntegerEnumeration.enumerate(VkDebugUtilsMessageTypeFlagEXT.class, type);
			message(messageSeverity, messageTypes, pCallbackData);
			return false;
		}

		/**
		 * Delegate method handler for an incoming message.
		 * @param severity		Message severity
		 * @param types			Type(s)
		 * @param data			Additional data
		 */
		protected abstract void message(VkDebugUtilsMessageSeverityFlagEXT severity, Set<VkDebugUtilsMessageTypeFlagEXT> types, VkDebugUtilsMessengerCallbackDataEXT data);
	}

	/**
	 * Creates a standard message callback that formats a debug message to a human-readable string which is output to the given writer.
	 * <p>
	 * The resultant message has the following format: <code>severity:types:id:message</code> where:
	 * <ul>
	 * <li><i>severity</i> - message severity, e.g. <code>WARN</code></li>
	 * <li><i>types</i> - message types(s) as a compound slash-delimited token, e.g. <code>GENERAL-PERFORMANCE</code></li>
	 * <li><i>id</i> - message identifier</li>
	 * <li><i>message</i> - description</li>
	 * </ul>
	 * @param out Output writer
	 * @return New callback
	 */
	public static MessageCallback writer(PrintWriter out) {
		return new AbstractMessageCallback() {
			@Override
			protected void message(VkDebugUtilsMessageSeverityFlagEXT severity, Set<VkDebugUtilsMessageTypeFlagEXT> types, VkDebugUtilsMessengerCallbackDataEXT data) {
				// Build compound types token
				final String compoundTypes = types.stream().map(this::toString).collect(joining("-"));

				// Build message
				final String message = new StringJoiner(":")
					.add(toString(severity))
					.add(compoundTypes)
					.add(data.pMessageIdName)
					.add(data.pMessage)
					.toString();

				// Output
				out.println(message);
				out.flush();
			}

			/**
			 * Helper - Converts the given message severity to a human-readable token.
			 * @param severity Message severity
			 * @return Severity token
			 */
			private String toString(VkDebugUtilsMessageSeverityFlagEXT severity) {
				return switch(severity) {
				case VK_DEBUG_UTILS_MESSAGE_SEVERITY_ERROR_BIT_EXT -> "ERROR";
				case VK_DEBUG_UTILS_MESSAGE_SEVERITY_WARNING_BIT_EXT -> "WARN";
				case VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT -> "INFO";
				case VK_DEBUG_UTILS_MESSAGE_SEVERITY_VERBOSE_BIT_EXT -> "VERBOSE";
				default -> String.valueOf(severity.value());
				};
			}

			/**
			 * Helper - Converts the given message type to a human readable token.
			 * @param type Message type
			 * @return Message type token
			 */
			private String toString(VkDebugUtilsMessageTypeFlagEXT type) {
				return switch(type) {
				case VK_DEBUG_UTILS_MESSAGE_TYPE_VALIDATION_BIT_EXT -> "VALIDATION";
				case VK_DEBUG_UTILS_MESSAGE_TYPE_GENERAL_BIT_EXT -> "GENERAL";
				case VK_DEBUG_UTILS_MESSAGE_TYPE_PERFORMANCE_BIT_EXT -> "PERFORMANCE";
				default -> String.valueOf(type.value());
				};
			}
		};
	}

	/**
	 * Standard message callback that formats and dumps messages to the console.
	 * @see #writer(PrintWriter)
	 */
	public static final MessageCallback CONSOLE = writer(new PrintWriter(System.out));

	private final MessageCallback callback;
	private final Pointer data;
	private final Set<VkDebugUtilsMessageSeverityFlagEXT> severities;
	private final Set<VkDebugUtilsMessageTypeFlagEXT> types;

	/**
	 * Constructor.
	 * @param callback			Callback handler
	 * @param data				Optional user data
	 * @param severities		Support message severity
	 * @param types				Supported message types
	 */
	public MessageHandler(MessageCallback callback, Pointer data, Set<VkDebugUtilsMessageSeverityFlagEXT> severities, Set<VkDebugUtilsMessageTypeFlagEXT> types) {
		this.callback = notNull(callback);
		this.data = data;
		this.severities = Set.copyOf(notEmpty(severities));
		this.types = Set.copyOf(notEmpty(types));
	}

	/**
	 * @return Creation descriptor for this handler
	 */
	protected VkDebugUtilsMessengerCreateInfoEXT create() {
		final VkDebugUtilsMessengerCreateInfoEXT info = new VkDebugUtilsMessengerCreateInfoEXT();
		info.messageSeverity = IntegerEnumeration.mask(severities);
		info.messageType = IntegerEnumeration.mask(types);
		info.pfnUserCallback = callback;
		info.pUserData = data;
		return info;
	}

	/**
	 * Builder for a message handler.
	 */
	public static class Builder {
		private MessageCallback callback = CONSOLE;
		private Pointer data;
		private final Set<VkDebugUtilsMessageSeverityFlagEXT> severities = new HashSet<>();
		private final Set<VkDebugUtilsMessageTypeFlagEXT> types = new TreeSet<>();

		/**
		 * Sets the callback handler.
		 * @param callback Callback handler, default is {@link MessageHandler#CONSOLE}
		 */
		public Builder callback(MessageCallback callback) {
			this.callback = notNull(callback);
			return this;
		}

		/**
		 * Sets the optional user data.
		 * @param data User data
		 */
		public Builder data(Pointer data) {
			this.data = notNull(data);
			return this;
		}

		/**
		 * Adds a message severity handled by this handler.
		 * @param severity Message severity
		 */
		public Builder severity(VkDebugUtilsMessageSeverityFlagEXT severity) {
			Check.notNull(severity);
			severities.add(severity);
			return this;
		}

		/**
		 * Adds a message type handled by this handler.
		 * @param type Message type
		 */
		public Builder type(VkDebugUtilsMessageTypeFlagEXT type) {
			Check.notNull(type);
			types.add(type);
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
		 * Constructs this handler.
		 * @return New message handler
		 */
		public MessageHandler build() {
			return new MessageHandler(callback, data, severities, types);
		}
	}
}
