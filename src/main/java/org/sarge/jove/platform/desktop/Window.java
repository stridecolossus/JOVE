package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireNotEmpty;

import java.util.*;
import java.util.function.Supplier;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.NativeReference;
import org.sarge.jove.platform.desktop.Desktop.MainThread;
import org.sarge.lib.LazySupplier;

/**
 * Native window implemented using GLFW.
 * @author Sarge
 */
public final class Window extends TransientNativeObject {
	/**
	 * Window creation hints.
	 */
	@SuppressWarnings("unused")
	// TODO - use enabled to toggle?
	// TODO - to IntEnum?
	public enum Hint {
		/**
		 * Window can be resized.
		 */
		RESIZABLE(0x00020003, true),

		/**
		 * Window has standard decorations (border, close icon, etc).
		 */
		DECORATED(0x00020005, true),

		/**
		 * Full screen windows are iconified on focus loss.
		 */
		AUTO_ICONIFY(0x00020006, true),

		/**
		 * Window is initially maximised (ignores dimensions).
		 */
		MAXIMISED(0x00020008, false),

		// TODO
		VISIBLE(0x00020004, true),

		/**
		 * Client API for this window, e.g. OpenGL context.
		 */
		CLIENT_API(0x00022001, false); // TODO - 0x00030001 = OPENGL_API

		private final int hint;
		private final boolean enabled;

		private Hint(int hint, boolean enabled) {
			this.hint = hint;
			this.enabled = enabled;
		}

		/**
		 * Applies this hint.
		 * @param lib Desktop library
		 */
		void apply(DesktopLibrary lib, int arg) {
			lib.glfwWindowHint(hint, arg);
		}
	}

	private final Desktop desktop;
	private final Supplier<KeyboardDevice> keyboard = new LazySupplier<>(() -> new KeyboardDevice(this));
	private final Supplier<MouseDevice> mouse = new LazySupplier<>(() -> new MouseDevice(this));
	//private final Map<Object, Callback> registry = new WeakHashMap<>();

	/**
	 * Constructor.
	 * @param window	Window handle
	 * @param desktop	Desktop service
	 */
	Window(Handle window, Desktop desktop) {
		super(window);
		this.desktop = requireNonNull(desktop);
	}

	/**
	 * @return Desktop service
	 */
	public Desktop desktop() {
		return desktop;
	}

	/**
	 * @return New keyboard device
	 */
	public KeyboardDevice keyboard() {
		return keyboard.get();
	}

	/**
	 * @return New mouse device
	 */
	public MouseDevice mouse() {
		return mouse.get();
	}

	/**
	 * @return Size of this window
	 */
	@MainThread
	public Dimensions size() {
		final NativeReference<Integer> w = desktop.factory().integer();
		final NativeReference<Integer> h = desktop.factory().integer();
		desktop.library().glfwGetWindowSize(this, w, h);
		return new Dimensions(w.get(), h.get());
	}

	/**
	 * Sets the window dimensions.
	 * @param size Window dimensions
	 */
	@MainThread
	public void size(Dimensions size) {
		desktop.library().glfwSetWindowSize(this, size.width(), size.height());
	}

	/**
	 * Resets the window title.
	 * @param title New title
	 */
	@MainThread
	public void title(String title) {
		requireNonNull(title);
		desktop.library().glfwSetWindowTitle(this, title);
	}

//	/**
//	 * @return Monitor for a full screen window
//	 */
//	@MainThread
//	public Optional<Monitor> monitor() {
//		final Monitor monitor =	desktop.library().glfwGetWindowMonitor(this);
//		return Optional.ofNullable(monitor);
//	}
//
//	/**
//	 * Sets this as a full screen window.
//	 */
//	@MainThread
//	public void full() {
//		// TODO - GLFWAPI void glfwSetWindowMonitor(GLFWwindow* window, GLFWmonitor* monitor, int xpos, int ypos, int width, int height, int refreshRate);
//		// monitor = null for windowed
//		// rate can be GLFW_DONTCARE
//		throw new UnsupportedOperationException();
//	}
//
//	/**
//	 * Sets the listener for window state changes.
//	 * @param listener Listener for window state changes or {@code null} to remove the listener
//	 */
//	@MainThread
//	public void listener(WindowListener.Type type, WindowListener listener) {
//		// Determine listener registration method
//		final DesktopLibrary lib = desktop.library();
//		final BiConsumer<Window, WindowStateListener> method = switch(type) {
//			case ENTER -> lib::glfwSetCursorEnterCallback;
//			case FOCUS -> lib::glfwSetWindowFocusCallback;
//			case ICONIFIED -> lib::glfwSetWindowIconifyCallback;
//			case CLOSED -> lib::glfwSetWindowCloseCallback;
//		};
//
//		// Register listener
//		final String key = "state";
//		if(listener == null) {
//			method.accept(this, null);
//			remove(key);
//		}
//		else {
//			final WindowStateListener adapter = (ptr, state) -> listener.state(type, NativeBooleanConverter.toBoolean(state));
//			method.accept(this, adapter);
////			register(type, adapter);
//		}
//	}
//
//	/**
//	 * Sets the listener for window resize events.
//	 * @param listener Resize listener or {@code null} to remove the listener
//	 */
//	@MainThread
//	public void resize(IntBinaryOperator listener) {
//		final String key = "resize";
//		final DesktopLibrary lib = desktop.library();
//		if(listener == null) {
//			lib.glfwSetWindowSizeCallback(this, null);
//			remove(key);
//		}
//		else {
//			final WindowResizeListener adapter = (ptr, w, h) -> listener.applyAsInt(w, h);
//			lib.glfwSetWindowSizeCallback(this, adapter);
////			register(key, adapter);
//		}
//	}
//
//	/**
//	 * Registers a JNA callback listener attached to this window.
//	 * <p>
//	 * Callbacks are <i>weakly</i> referenced by the given key preventing listeners being garbage collected and thus de-registered by GLFW.
//	 * <p>
//	 * @param key			Key
//	 * @param callback 		Callback listener
//	 */
////	protected void register(Object key, Callback callback) {
////		registry.put(key, callback);
////	}
//
//	/**
//	 * Removes a registry entry.
//	 * @param key Key
//	 */
//	protected void remove(Object key) {
////		registry.remove(key);
//	}

	/**
	 * Creates a Vulkan rendering surface for this window.
	 * @param instance Vulkan instance
	 * @return Vulkan surface
	 * @throws RuntimeException if the surface cannot be created for this window
	 */
	public Handle surface(Handle instance) {
		final DesktopLibrary lib = desktop.library();
		final NativeReference<Handle> ref = desktop.factory().pointer();
		final int result = lib.glfwCreateWindowSurface(instance, this, null, ref);
		if(result != 0) {
System.out.println("GLFW error "+Integer.toString(Math.abs(result), 16));
			final int code = lib.glfwGetError(null); // TODO - by-reference error string, managed by GLFW (!)
			System.out.println(code);
			throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
		}
		return ref.get();
	}

	@Override
	@MainThread
	protected void release() {
		desktop.library().glfwDestroyWindow(this);
	}

	/**
	 * Builder for a window.
	 */
	public static class Builder {
		private String title;
		private Dimensions size;
		private final Map<Hint, Integer> hints = new HashMap<>();
//		private Monitor monitor;

		/**
		 * Sets the window title.
		 * @param title Title
		 */
		public Builder title(String title) {
			this.title = requireNotEmpty(title);
			return this;
		}

		/**
		 * Sets the size of the window.
		 * @param size Window size
		 */
		public Builder size(Dimensions size) {
			this.size = requireNonNull(size);
			return this;
		}

		/**
		 * Adds a window hint.
		 * @param hint Window hint
		 */
		public Builder hint(Hint hint, int argument) {
			requireNonNull(hint);
			hints.put(hint, argument);
			return this;
		}
		// TODO - error if arg=1 and enabled by default? or add all enabled and REMOVE? i.e. toggles

//		public Builder hint(Hint hint, boolean enable) {
//			return hint(hint, NativeBooleanConverter.toInteger(enable));
//		}

//		/**
//		 * Sets the monitor for a full screen window.
//		 * @param monitor Monitor
//		 * @see Monitor#monitors(Desktop)
//		 */
//		public Builder monitor(Monitor monitor) {
//			this.monitor = requireNonNull(monitor);
//			return this;
//		}

		/**
		 * Constructs this window.
		 * @return New window
		 * @throws RuntimeException if the window cannot be created
		 */
		@MainThread
		public Window build(Desktop desktop) {
			// Apply window hints
			final DesktopLibrary lib = desktop.library();
			lib.glfwDefaultWindowHints();
			for(var entry : hints.entrySet()) {
				final Hint hint = entry.getKey();
				final int arg = entry.getValue();
				hint.apply(lib, arg);
			}

			// Create window
			final Handle window = lib.glfwCreateWindow(size.width(), size.height(), title, null/*monitor*/, null);
			if(window == null) throw new RuntimeException("Window cannot be created");

			// Create domain object
			return new Window(window, desktop);
		}
	}
}
