# Domain

```java
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

	private final MessageCallback callback;
	private final Set<VkDebugUtilsMessageSeverityFlagEXT> severities;
	private final Set<VkDebugUtilsMessageTypeFlagEXT> types;

	/**
	 * Constructor.
	 * @param callback			Callback handler
	 * @param severities		Support message severity
	 * @param types				Supported message types
	 */
	public MessageHandler(MessageCallback callback, Set<VkDebugUtilsMessageSeverityFlagEXT> severities, Set<VkDebugUtilsMessageTypeFlagEXT> types) {
	}
}
```

# Builder

```java
	public static class Builder {
		private MessageCallback callback;
		private final Set<VkDebugUtilsMessageSeverityFlagEXT> severities = new HashSet<>();
		private final Set<VkDebugUtilsMessageTypeFlagEXT> types = new TreeSet<>();

		/**
		 * Sets the callback handler.
		 * @param callback Callback handler, default is {@link MessageHandler#CONSOLE}
		 */
		public Builder callback(MessageCallback callback) {
		}

		/**
		 * Adds a message severity handled by this handler.
		 * @param severity Message severity
		 */
		public Builder severity(VkDebugUtilsMessageSeverityFlagEXT severity) {
		}

		/**
		 * Adds a message type handled by this handler.
		 * @param type Message type
		 */
		public Builder type(VkDebugUtilsMessageTypeFlagEXT type) {
		}

		/**
		 * Constructs this handler.
		 * @return New message handler
		 */
		public MessageHandler build() {
		}
	}
}
```

# Handlers

```java
public class Instance {
	private final Map<MessageHandler, Pointer> handlers = new HashMap<>();
	
	...
	
	/**
	 * Adds a diagnostics message handler to this instance.
	 * @param handler Message handler
	 * @throws IllegalArgumentException if the handler has already been added to this instance
	 */
	public void add(MessageHandler handler) {
	}

	/**
	 * Removes a diagnostics message handler from this instance.
	 * @param handler Message handler to remove
	 * @throws IllegalArgumentException if the handler is not present or has already been removed
	 */
	public void remove(MessageHandler handler) {
	}

	/**
	 * Destroys this instance.
	 */
	public void destroy() {
		// TODO - destroy handlers
		api.vkDestroyInstance(handle, null);
	}
```

# Handler

```java
public class MessageHandler {
	public interface Callback extends com.sun.jna.Callback {
		boolean message(int severity, int type, VkDebugUtilsMessengerCallbackDataEXT pCallbackData, Pointer pUserData);
	}
	
	public MessageHandler(Callback callback, Pointer data, Set<VkDebugUtilsMessageSeverityFlagEXT> severities, Set<VkDebugUtilsMessageTypeFlagEXT> types) {
	}
}
```

# Add handler

```java
	public synchronized void add(MessageHandler handler) {
		// Check handler is valid
		Check.notNull(handler);
		if(handlers.containsKey(handler)) throw new IllegalArgumentException("Duplicate message handler: " + handler);

		// Init factory
		if(factory == null) {
			factory = new HandlerFactory();
		}

		// Create and register handler
		final Pointer handle = factory.create(handler);
		handlers.put(handler, handle);
	}
```
# Helper

```java
	public Function function(String name) {
		final Pointer ptr = vulkan.api().vkGetInstanceProcAddr(handle, name);
		if(ptr == null) throw new ServiceException("Cannot find function pointer: " + name);
		return Function.getFunction(ptr);
	}
```

# Factory

```java
	private class HandlerFactory {
		private final Function create;
		private final Function destroy;

		private HandlerFactory() {
			this.create = function("vkCreateDebugUtilsMessengerEXT");
			this.destroy = function("vkDestroyDebugUtilsMessengerEXT");
		}

		/**
		 * @param name Function name
		 * @return Create messenger function pointer
		 */
		private Function function(String name) {
			final Pointer ptr = vulkan.api().vkGetInstanceProcAddr(Instance.this.handle, name);
			if(ptr == null) throw new ServiceException("Cannot find debug function pointer: " + name);
			return Function.getFunction(ptr);
		}

		/**
		 * Creates a message handler.
		 * @param handler Message handler descriptor
		 * @return Handle
		 */
		private Pointer create(MessageHandler handler) {
			final VkDebugUtilsMessengerCreateInfoEXT info = handler.create();
			final PointerByReference handle = vulkan.factory().reference();
			final Object[] args = {Instance.this.handle, info, null, handle};
			VulkanLibrary.check(create.invokeInt(args));
			return handle.getValue();
		}

		/**
		 * Destroys a message handler.
		 * @param handle Handle
		 */
		private void destroy(Pointer handle) {
			destroy.invoke(new Object[]{Instance.this.handle, handle, null});
		}
	}
```
