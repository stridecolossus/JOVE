package org.sarge.jove.platform.desktop;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.lang.foreign.MemorySegment;
import java.util.*;
import java.util.function.BiConsumer;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.desktop.Desktop.MainThread;

/**
 * Native window implemented using GLFW.
 * @author Sarge
 */
public class Window extends AbstractNativeObject {
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

	/**
	 * Convenience aggregation of the mouse devices.
	 */
	public record Mouse(MouseButtons buttons, MousePointer pointer, MouseWheel wheel) {
	}

	private final Keyboard keyboard = new Keyboard(this);
	private final Mouse mouse = new Mouse(new MouseButtons(this), new MousePointer(this), new MouseWheel(this));
	private final WindowLibrary library;

	/**
	 * Constructor.
	 * @param window	Window handle
	 * @param library	Window library
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
	 * @return Keyboard device for this window
	 */
	public Keyboard keyboard() {
		return keyboard;
	}

	/**
	 * @return Mouse devices for this window
	 */
	public Mouse mouse() {
		return mouse;
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

	/**
	 * Listener for window events represented by a boolean state, e.g. window focus.
	 */
	@FunctionalInterface
	public interface WindowStateListener extends Callback {
		/**
		 * Listener event type.
		 */
		enum Type {
			ENTER,
			FOCUS,
			MINIMISE,
			CLOSE
		}

		/**
		 * Notifies that a window state change.
		 * @param window		Window
		 * @param state			State
		 */
		void state(MemorySegment window, int state);
	}

	/**
	 * Sets the listener for window state changes.
	 * @param listener Listener for window state changes or {@code null} to remove the listener
	 */
	@MainThread
	public void listener(WindowStateListener.Type type, WindowStateListener listener) {
		final BiConsumer<Window, WindowStateListener> method = switch(type) {
			case ENTER		-> library::glfwSetCursorEnterCallback;
			case FOCUS		-> library::glfwSetWindowFocusCallback;
			case MINIMISE	-> library::glfwSetWindowIconifyCallback;
			case CLOSE		-> library::glfwSetWindowCloseCallback;
		};
		method.accept(this, listener);
	}

	/**
	 * Listener for window resize events.
	 */
	public interface WindowResizeListener extends Callback {
		/**
		 * Notifies a window resize event.
		 * @param window		Window
		 * @param width			Width
		 * @param height		Height
		 */
		void resize(MemorySegment window, int width, int height);
	}

	/**
	 * Sets the listener for window resize events.
	 * @param listener Resize listener or {@code null} to remove the listener
	 */
	@MainThread
	public void resize(WindowResizeListener listener) {
		library.glfwSetFramebufferSizeCallback(this, listener);
	}

	@Override
	@MainThread
	protected void release() {
		library.glfwDestroyWindow(this);
	}

	@Override
	public String toString() {
		return String.format("Window[%s]", this.handle());
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
