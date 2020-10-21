package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * GLFW window.
 * @author Sarge
 */
public class Window {
	/**
	 * Creation descriptor for a window.
	 */
	public static final class Descriptor {
		/**
		 * Window properties.
		 */
		public enum Property {
			/**
			 * Window can be resized.
			 */
			RESIZABLE,

			/**
			 * Window has standard decorations (border, close icon, etc).
			 */
			DECORATED,

			/**
			 * Full-screen windows are iconified on focus loss.
			 */
			AUTO_ICONIFY,

			/**
			 * Window is initially maximised (ignores dimensions).
			 */
			MAXIMISED,

			/**
			 * Disables creation of an OpenGL context for this window.
			 */
			DISABLE_OPENGL,

			/**
			 * Whether this window should be full-screen.
			 */
			FULL_SCREEN,
		}

		private final String title;
		private final Dimensions size;
		private final Optional<Monitor> monitor;
		private final Set<Property> props;

		/**
		 * Constructor.
		 * @param title			Window title
		 * @param size			Size
		 * @param monitor		Monitor
		 * @param props			Properties
		 */
		public Descriptor(String title, Dimensions size, Monitor monitor, Set<Property> props) {
			this.title = notEmpty(title);
			this.size = notNull(size);
			this.monitor = Optional.ofNullable(monitor);
			this.props = Set.copyOf(props);
		}

		/**
		 * @return Window title
		 */
		public String title() {
			return title;
		}

		/**
		 * @return Size of this window
		 */
		public Dimensions size() {
			return size;
		}

		/**
		 * @return Monitor for this window
		 */
		public Optional<Monitor> monitor() {
			return monitor;
		}

		public Set<Property> properties() {
			return props;
		}

		/**
		 * Builder for a window descriptor.
		 */
		public static class Builder {
			private String title;
			private Dimensions size;
			private Monitor monitor;
			private final Set<Property> props = new HashSet<>();

			/**
			 * Sets the window title.
			 * @param title Title
			 */
			public Builder title(String title) {
				this.title = title;
				return this;
			}

			/**
			 * Sets the size of the window.
			 * @param size Window size
			 */
			public Builder size(Dimensions size) {
				this.size = size;
				return this;
			}

			/**
			 * Sets the monitor for the window.
			 * @param monitor Monitor
			 */
			public Builder monitor(Monitor monitor) {
				this.monitor = monitor;
				return this;
			}

			/**
			 * Adds a window property.
			 * @param p Property
			 */
			public Builder property(Property p) {
				props.add(p);
				return this;
			}

			/**
			 * Constructs this descriptor.
			 * @param New descriptor
			 */
			public Descriptor build() {
				return new Descriptor(title, size, monitor, props);
			}
		}
	}

	private final Handle handle;
	private final DesktopLibrary instance;
	private final Descriptor props;
//	private final Device<?> device;
	private final Pointer ptr;

	/**
	 * Constructor.
	 * @param window		Window handle
	 * @param instance		GLFW API
	 * @param props			Window properties
	 */
	Window(Pointer window, DesktopLibrary instance, Descriptor props) {
		this.handle = new Handle(window);
		this.instance = notNull(instance);
		this.props = notNull(props);
//		this.device = null; // TODO - createDevice(window, instance);
		this.ptr = notNull(window); // TODO
	}

	// TODO
	public void setMouseMoveListener(MousePositionListener listener) {
		instance.glfwSetCursorPosCallback(ptr, listener);
	}
	public void setKeyListener(KeyListener listener) {
		instance.glfwSetKeyCallback(ptr, listener);
	}

	public Descriptor descriptor() {
		return props;
	}

//	public Device<?> device() {
//		return null;
////		return device;
//	}

	public void poll() {
		instance.glfwPollEvents();
	}

//	/**
//	 * Creates the device for this window.
//	 * @param window		Window
//	 * @param instance		GLFW instance
//	 * @return New device
//	 */
//	private static Device<?> createDevice(Pointer window, FrameworkLibrary instance) {
//		final Map<Event.Category, Device.Entry<Pointer, ?>> map = Map.of(
//			Event.Category.BUTTON,	new Device.Entry<>(FrameworkWindow::key, instance::glfwSetKeyCallback),
//			Event.Category.MOVE, 	new Device.Entry<>(FrameworkWindow::move, instance::glfwSetCursorPosCallback),
//			Event.Category.CLICK, 	new Device.Entry<>(FrameworkWindow::button, instance::glfwSetMouseButtonCallback),
//			Event.Category.ZOOM, 	new Device.Entry<>(FrameworkWindow::scroll, instance::glfwSetScrollCallback)
//		);
//		return new Device<>(window, map);
//	}
//
//	/**
//	 * Creates a GLFW key listener.
//	 * @param handler Event handler
//	 * @return Key listener
//	 */
//	private static KeyListener key(Event.Handler handler) {
//		return new KeyListener() {
//			@Override
//			public void key(Pointer window, int num, int scancode, int action, int mods) {
//				final Event.Operation op = FrameworkHelper.operation(action);
//				final Event.Descriptor descriptor = new Event.Descriptor(Event.Category.BUTTON, num, op);
//				final Event event = new Event(descriptor);
//				handler.handle(event);
//			}
//		};
//	}
//
//	/**
//	 * Creates a GLFW mouse movement listener.
//	 * @param handler Event handler
//	 * @return Mouse movement listener
//	 */
//	private static MousePositionListener move(Event.Handler handler) {
//		return (window, x, y) -> {
//			final Event event = new Event(Event.Descriptor.MOVE, (int) x, (int) y);
//			handler.handle(event);
//		};
//	}
//
//	/**
//	 * Creates a GLFW mouse button listener.
//	 * @param handler Event handler
//	 * @return Mouse button listener
//	 */
//	private static MouseButtonListener button(Event.Handler handler) {
//		return (window, button, action, mods) -> {
//			final Event.Operation op = FrameworkHelper.operation(action);
//			final Event.Descriptor descriptor = new Event.Descriptor(Event.Category.CLICK, button, op);
//			final Event event = new Event(descriptor);
//			handler.handle(event);
//		};
//	}
//
//	/**
//	 * Creates a GLFW mouse scroll listener.
//	 * @param handler Event handler
//	 * @return Mouse scroll listener
//	 */
//	private static MouseScrollListener scroll(Event.Handler handler) {
//		return (window, x, y) -> {
//			final Event event = new Event(Event.Descriptor.ZOOM, (int) x, (int) y);
//			handler.handle(event);
//		};
//	}

	public Handle surface(Handle vulkan) {
		final PointerByReference ref = new PointerByReference();
		final int result = instance.glfwCreateWindowSurface(vulkan, handle, null, ref);
		if(result != 0) {
			throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
		}
		return new Handle(ref.getValue());
	}

	public void destroy() {
		instance.glfwDestroyWindow(handle);
	}
}
