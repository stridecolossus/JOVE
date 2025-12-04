package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.desktop.Desktop.MainThread;

/**
 * Native window implemented using GLFW.
 * @author Sarge
 */
public class Window extends TransientNativeObject {
	/**
	 * Window creation hints.
	 */
	public enum Hint {
		/**
		 * Window can be resized.
		 */
		RESIZABLE(0x00020003),

		/**
		 * Window has standard decorations (border, close icon, etc).
		 */
		DECORATED(0x00020005),

		/**
		 * Full screen windows are iconified on focus loss.
		 */
		AUTO_ICONIFY(0x00020006),

		/**
		 * Window is initially maximised (ignores dimensions).
		 */
		MAXIMISED(0x00020008),

		/**
		 * Window is initially visible.
		 */
		VISIBLE(0x00020004),

		/**
		 * Whether a client API is created for the window, i.e. an OpenGL context.
		 * Note by default GLFW <b>enables</b> this hint.
		 */
		CLIENT_API(0x00022001);

		private final int code;

		private Hint(int code) {
			this.code = code;
		}
	}

	// TODO - was lazy supplier!!!
	private final WindowLibrary library;
	private final Map<Object, Callback> listeners = new WeakHashMap<>();

//	public enum Type {
//		ENTER,
//		FOCUS,
//		ICONIFIED,
//		CLOSED
//	}

	/**
	 * Constructor.
	 * @param window	Window handle
	 * @param desktop	Desktop service
	 */
	Window(Handle window, WindowLibrary library) {
		super(window);
		this.library = requireNonNull(library);
	}

	/**
	 * @return GLFW library
	 */
	WindowLibrary library() {
		return library;
	}

	/**
	 * Unit for window dimensions.
	 */
	public enum Unit {
		PIXEL,
		SCREEN_COORDINATE
	}

	/**
	 * @param unit Window size unit
	 * @return Size of this window
	 */
	@MainThread
	public Dimensions size(Unit unit) {
		final var w = new IntegerReference();
		final var h = new IntegerReference();
		switch(unit) {
			case PIXEL				-> library.glfwGetFramebufferSize(this, w, h);
			case SCREEN_COORDINATE	-> library.glfwGetWindowSize(this, w, h);
		}
		return new Dimensions(w.get(), h.get());
	}

	/**
	 * Sets the window dimensions.
	 * @param size Window dimensions
	 */
	@MainThread
	public void size(Dimensions size) {
		library.glfwSetWindowSize(this, size.width(), size.height());
	}
	// TODO - unit?

	/**
	 * Sets the window title.
	 * @param title New title
	 */
	@MainThread
	public void title(String title) {
		requireNonNull(title);
		library.glfwSetWindowTitle(this, title);
	}

	/**
	 * Creates a Vulkan rendering surface for this window.
	 * @param instance Vulkan instance
	 * @return Vulkan surface
	 * @throws RuntimeException if the surface cannot be created
	 * @see Desktop#error()
	 */
	public Handle surface(Handle instance) {
		final var pointer = new Pointer();
		final int result = library.glfwCreateWindowSurface(instance, this, null, pointer);
		if(result != 0) {
			throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
		}
		return pointer.handle();
	}

//	/**
//	 * @return Monitor for a full screen window
//	 */
//	@MainThread
//	public Optional<Monitor> monitor() {
//		final Monitor monitor =	library.glfwGetWindowMonitor(this);
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

	/**
	 * @return Listeners attached to this window
	 */
	protected Map<Object, Callback> listeners() {
		return Collections.unmodifiableMap(listeners);
	}

	/**
	 * Registers a device listener attached to this window.
	 * <p>
	 * Callbacks are <i>weakly</i> referenced by the given key preventing listeners being garbage collected and thus unregistered by GLFW.
	 * <p>
	 * @param key			Key
	 * @param listener 		Listener
	 */
	protected void register(Object key, Callback listener) {
		requireNonNull(key);
		requireNonNull(listener);
		listeners.put(key, listener);
	}

	/**
	 * Removes a registry entry.
	 * @param key Key
	 */
	protected void remove(Object key) {
		listeners.remove(key);
	}

	@Override
	@MainThread
	protected void release() {
		listeners.clear();
		library.glfwDestroyWindow(this);
	}

	/**
	 * Builder for a window.
	 */
	public static class Builder {
		private String title;
		private Dimensions size;
		private final Map<Integer, Integer> hints = new HashMap<>();
		private Monitor monitor;

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
		 * @param hint			Window hint
		 * @param argument		Argument
		 */
		public Builder hint(Hint hint, int argument) {
			return hint(hint.code, argument);
		}

		/**
		 * Adds a window hint.
		 * @param hint			Window hint
		 * @param argument		Argument
		 */
		public Builder hint(int hint, int argument) {
			hints.put(hint, argument);
			return this;
		}

		/**
		 * Sets the monitor for a full screen window.
		 * @param monitor Monitor
		 */
		public Builder monitor(Monitor monitor) {
			this.monitor = monitor;
			return this;
		}

		/**
		 * Constructs this window.
		 * @return New window
		 * @throws RuntimeException if the window cannot be created
		 * @see Desktop#error()
		 */
		@MainThread
		public Window build(Desktop desktop) {
			// Reset window hints
			final WindowLibrary library = desktop.library();
			library.glfwDefaultWindowHints();

			// Apply window hints
			for(var entry : hints.entrySet()) {
				library.glfwWindowHint(entry.getKey(), entry.getValue());
			}

			// Create window
			final Handle window = library.glfwCreateWindow(size.width(), size.height(), title, monitor, null);
			if(window == null) {
				throw new RuntimeException("Window could not be created");
			}

			// Create domain object
			return new Window(window, library);
		}
	}
}
