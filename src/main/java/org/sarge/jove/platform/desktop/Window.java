package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notNull;

import java.util.HashSet;
import java.util.Set;
import java.util.WeakHashMap;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Native window implemented using GLFW.
 * @author Sarge
 */
public class Window extends AbstractTransientNativeObject {
	/**
	 * Window properties.
	 */
	public enum Property {
		/**
		 * Window can be resized.
		 */
		RESIZABLE(0x00020003),

		/**
		 * Window has standard decorations (border, close icon, etc).
		 */
		DECORATED(0x00020005),

		/**
		 * Full-screen windows are iconified on focus loss.
		 */
		AUTO_ICONIFY(0x00020006),

		/**
		 * Window is initially maximised (ignores dimensions).
		 */
		MAXIMISED(0x00020008),

		/**
		 * Disables creation of an OpenGL context for this window.
		 */
		DISABLE_OPENGL(0x00022001),

		/**
		 * Whether this window should be full-screen.
		 */
		FULL_SCREEN(0);		// TODO

		private final int hint;

		private Property(int hint) {
			this.hint = hint;
		}

		/**
		 * Applies this property.
		 * @param lib Desktop library
		 */
		void apply(DesktopLibrary lib) {
			final int value = this == DISABLE_OPENGL ? 0 : 1; // TODO
			lib.glfwWindowHint(hint, value);
		}
		// TODO - probably need different implementations for hints, disable OpenGL, full-screen, etc
	}

	/**
	 * Descriptor for the properties of a window.
	 */
	public record Descriptor(String title, Dimensions size, Set<Property> properties) {
		public Descriptor {
			Check.notEmpty(title);
			Check.notNull(size);
			Check.notNull(properties);
		}
	}

	/**
	 * Creates a GLFW window.
	 * @param lib				GLFW library
	 * @param descriptor		Window descriptor
	 * @param monitor			Optional monitor
	 * @return New window
	 * @throws RuntimeException if the window cannot be created
	 */
	public static Window create(DesktopLibrary lib, Descriptor descriptor, Monitor monitor) {
		// TODO
		if(monitor != null) throw new UnsupportedOperationException();

		// Apply window hints
		lib.glfwDefaultWindowHints();
		descriptor.properties().forEach(p -> p.apply(lib));

		// Create window
		final Dimensions size = descriptor.size();
		final Pointer window = lib.glfwCreateWindow(size.width(), size.height(), descriptor.title(), null, null);	// TODO - monitor
		if(window == null) {
			throw new RuntimeException(String.format("Window cannot be created: descriptor=%s monitor=%s", descriptor, monitor));
		}

		// Create window wrapper
		return new Window(window, lib, descriptor);
	}

	private final DesktopLibrary lib;
	private final Descriptor descriptor;
	private final WeakHashMap<Object, Object> registry = new WeakHashMap<>();

	/**
	 * Constructor.
	 * @param window			Window handle
	 * @param lib				GLFW API
	 * @param descriptor		Window descriptor
	 */
	Window(Pointer window, DesktopLibrary lib, Descriptor descriptor) {
		super(window);
		this.lib = notNull(lib);
		this.descriptor = notNull(descriptor);
	}

	/**
	 * @return Descriptor for this window
	 */
	public Descriptor descriptor() {
		return descriptor;
	}

	/**
	 * @return GLFW
	 */
	DesktopLibrary library() {
		return lib;
	}

	/**
	 * @return New keyboard device
	 */
	public KeyboardDevice keyboard() {
		return new KeyboardDevice(this);
	}

	/**
	 * @return New mouse device
	 */
	public MouseDevice mouse() {
		return new MouseDevice(this);
	}

	/**
	 * Registers a native callback bound to the given handler to prevent GC of callbacks.
	 * @param handler			Handler
	 * @param callback			Callback
	 */
	void register(Object handler, Object callback) {
		registry.put(handler, callback);
	}

	/**
	 * Creates a Vulkan rendering surface for this window.
	 * @param vulkan Vulkan instance handle
	 * @return Vulkan surface
	 */
	public Handle surface(Handle vulkan) {
		final PointerByReference ref = new PointerByReference();
		final int result = lib.glfwCreateWindowSurface(vulkan, this.handle(), null, ref);
		if(result != 0) {
			throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
		}
		return new Handle(ref.getValue());
	}

	@Override
	protected void release() {
		lib.glfwDestroyWindow(this.handle());
	}

	/**
	 * Builder for a window.
	 */
	public static class Builder {
		private final DesktopLibrary lib;
		private String title;
		private Dimensions size;
		private final Set<Property> props = new HashSet<>();

		/**
		 * Constructor.
		 * @param desktop Desktop service
		 */
		public Builder(Desktop desktop) {
			this.lib = desktop.library();
		}

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
		 * Adds a window property.
		 * @param p Property
		 */
		public Builder property(Property p) {
			props.add(p);
			return this;
		}

		/**
		 * Constructs this window.
		 * @param New window
		 */
		public Window build() {
			final var desc = new Descriptor(title, size, props);
			return Window.create(lib, desc, null);			// TODO - monitor
		}
	}
}
