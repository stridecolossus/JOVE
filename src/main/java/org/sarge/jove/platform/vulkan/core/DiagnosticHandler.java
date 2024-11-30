package org.sarge.jove.platform.vulkan.core;

import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;
import static org.sarge.lib.Validation.requireNotEmpty;

import java.lang.foreign.*;
import java.lang.invoke.*;
import java.util.*;
import java.util.function.Consumer;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.foreign.NativeStructure.StructureNativeTransformer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * A <i>handler</i> is a consumer for Vulkan diagnostic messages.
 * @author Sarge
 */
public class DiagnosticHandler extends TransientNativeObject {
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
	DiagnosticHandler(Handle handle, Instance instance) {
		super(handle);
		this.instance = requireNonNull(instance);
	}

	@Override
	protected void release() {
		final Handle function = instance.function("vkDestroyDebugUtilsMessengerEXT");
		final TransformerRegistry registry = instance.vulkan().registry();
		final NativeMethod destroy = destroy(function.address(), registry);
		final Object[] args = {instance, this, null};
		invoke(destroy, args);
	}

	/**
	 * @return Destroy method for this handler
	 */
	private static NativeMethod destroy(MemorySegment address, TransformerRegistry registry) {
		final Class<?>[] signature = {Instance.class, DiagnosticHandler.class, Handle.class};
		return new NativeMethod.Builder(registry)
				.address(address)
				.signature(signature)
				.build();
	}

	/**
	 * Helper - Invokes a function pointer.
	 * @param method		Native method
	 * @param args			Arguments
	 * @return Return value
	 */
	private static Object invoke(NativeMethod method, Object[] args) {
		return method.invoke(args, Arena.ofAuto());
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
			requireNonNull(severity);
			requireNotEmpty(types);
			requireNonNull(data);
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
	 * Note that the callback signature is not defined in the Vulkan API.
	 * <p>
	 * @see <a href="https://registry.khronos.org/vulkan/specs/1.3-extensions/man/html/PFN_vkDebugUtilsMessengerCallbackEXT.html">Vulkan documentation</a>
	 */
	private static class MessageCallback {
		private static final ReverseMapping<VkDebugUtilsMessageSeverity> SEVERITY = IntEnum.reverse(VkDebugUtilsMessageSeverity.class);
		private static final ReverseMapping<VkDebugUtilsMessageType> TYPE = IntEnum.reverse(VkDebugUtilsMessageType.class);

		private final Consumer<Message> consumer;
		private final NativeTransformer<NativeStructure, MemorySegment> transformer;

		/**
		 * Constructor.
		 * @param consumer		Message handler
		 * @param registry		Native mappers
		 */
		MessageCallback(Consumer<Message> consumer, TransformerRegistry registry) {
			this.consumer = requireNonNull(consumer);
			this.transformer = new StructureNativeTransformer(registry).derive(VkDebugUtilsMessengerCallbackData.class);
		}

		/**
		 * Callback handler method.
		 * @param severity			Severity
		 * @param type				Message type(s) bitfield
		 * @param pCallbackData		Data
		 * @param pUserData			Optional user data (always {@code null})
		 * @return {@code false}
		 */
		@SuppressWarnings("unused")
		public boolean message(int severity, int typeMask, MemorySegment pCallbackData, MemorySegment pUserData) {
			// TODO - remove once completely satisfied unmarshalling works ok
			System.err.println("VULKAN ERROR...");

			// Transform the message properties
			final var types = new BitMask<VkDebugUtilsMessageType>(typeMask).enumerate(TYPE);
			final var level = SEVERITY.map(severity);

			// Unmarshal the message structure
			final var data = (VkDebugUtilsMessengerCallbackData) transformer.returns().apply(pCallbackData);

			// Handle message
			final Message message = new Message(level, types, data);
			consumer.accept(message);

			return false;
		}

		/**
		 * @return Up-call address for this callback
		 */
		MemorySegment address() {
    		final MethodHandle handle = handle();
    		return link(handle.bindTo(this));
		}

		/**
		 * Builds the callback method handle.
		 */
		private static MethodHandle handle() {
			final var type = MethodType.methodType(boolean.class, int.class, int.class, MemorySegment.class, MemorySegment.class);
    		try {
        		return MethodHandles.lookup().findVirtual(MessageCallback.class, "message", type);
    		}
			catch(Exception e) {
				throw new RuntimeException("Error instantiating the diagnostic callback", e);
			}
		}

		/**
		 * Links the callback up-call stub.
		 */
		private static MemorySegment link(MethodHandle handle) {
    		final var descriptor = FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT, JAVA_INT, ADDRESS, ADDRESS);
    		try {
    			return Linker.nativeLinker().upcallStub(handle, descriptor, Arena.ofAuto());
    		}
			catch(Exception e) {
				throw new RuntimeException("Error instantiating the diagnostic callback", e);
			}
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
	 * <li>If not explicitly configured the severity and types are initialised to default values</li>
	 * <li>The default message consumer dumps diagnostic reports to the error console</li>
	 * </ul>
	 */
	public static class Builder {
		private final Instance instance;
		private final TransformerRegistry registry;
		private final Set<VkDebugUtilsMessageSeverity> severity = new HashSet<>();
		private final Set<VkDebugUtilsMessageType> types = new HashSet<>();
		private Consumer<Message> consumer = System.err::println;

		/**
		 * Constructor.
		 * @param instance Parent instance
		 */
		Builder(Instance instance) {
			this.instance = requireNonNull(instance);
			this.registry = instance.vulkan().registry();
		}

		/**
		 * Sets the message consumer (messages are dumped to the error console by default).
		 * @param consumer Message consumer
		 */
		public Builder consumer(Consumer<Message> consumer) {
			this.consumer = requireNonNull(consumer);
			return this;
		}

		/**
		 * Adds a message severity to be reported by this handler.
		 * @param severity Message severity
		 */
		public Builder severity(VkDebugUtilsMessageSeverity severity) {
			this.severity.add(requireNonNull(severity));
			return this;
		}

		/**
		 * Adds a message type to be reported by this handler.
		 * @param type Message type
		 */
		public Builder type(VkDebugUtilsMessageType type) {
			types.add(requireNonNull(type));
			return this;
		}

		/**
		 * Initialises the message configuration to default values if not provided.
		 */
		private void init() {
			if(severity.isEmpty()) {
				severity(VkDebugUtilsMessageSeverity.WARNING);
				severity(VkDebugUtilsMessageSeverity.ERROR);
			}
			if(types.isEmpty()) {
				type(VkDebugUtilsMessageType.GENERAL);
				type(VkDebugUtilsMessageType.VALIDATION);
			}
		}

		/**
		 * Builds this handler and attaches it to the given instance.
		 */
		public DiagnosticHandler build() {
			init();
			final var callback = new MessageCallback(consumer, registry);
			final var info = populate(callback.address());
			final Handle handle = create(info);
			return new DiagnosticHandler(handle, instance);
		}

		/**
		 * Builds the descriptor for this handler.
		 * @param callback Callback address
		 */
		private VkDebugUtilsMessengerCreateInfoEXT populate(MemorySegment callback) {
			final var info = new VkDebugUtilsMessengerCreateInfoEXT();
			info.messageSeverity = new BitMask<>(severity);
			info.messageType = new BitMask<>(types);
			info.pfnUserCallback = new Handle(callback);
			info.pUserData = null;
			return info;
		}

		/**
		 * Creates this handler.
		 */
		private Handle create(VkDebugUtilsMessengerCreateInfoEXT info) {
			final Handle create = instance.function("vkCreateDebugUtilsMessengerEXT");
			final NativeMethod method = method(create.address());
    		final PointerReference ref = instance.vulkan().factory().pointer();
			create(method, info, ref);
    		return ref.handle();
		}

		/**
		 * Builds the native method to create this handler.
		 */
		private NativeMethod method(MemorySegment address) {
    		return new NativeMethod.Builder(registry)
    				.address(address)
    				.returns(int.class)
    				.parameter(Instance.class)
    				.parameter(VkDebugUtilsMessengerCreateInfoEXT.class)
    				.parameter(Handle.class)
    				.parameter(PointerReference.class, true)
    				.build();
		}

		/**
		 * Invokes the create function.
		 */
		private void create(NativeMethod method, VkDebugUtilsMessengerCreateInfoEXT info, PointerReference ref) {
    		final Object[] args = {instance, info, null, ref};
    		Vulkan.check((int) DiagnosticHandler.invoke(method, args));
		}
	}
}
