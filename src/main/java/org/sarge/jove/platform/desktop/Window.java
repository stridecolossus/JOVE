package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notNull;

import java.util.HashSet;
import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.NativeObject.TransientNativeObject;
import org.sarge.jove.control.Device;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * Native window implemented using GLFW.
 * @author Sarge
 */
public class Window implements TransientNativeObject {
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
	 *
	 */
	public record Descriptor(String title, Dimensions size, Set<Property> properties) {
		/**
		 * Builder for a window descriptor.
		 */
		public static class Builder {
			private String title;
			private Dimensions size;
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
				return new Descriptor(title, size, props);
			}
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
	static Window create(DesktopLibrary lib, Descriptor descriptor, Monitor monitor) {
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

	private final Handle handle;
	private final DesktopLibrary lib;
	private final Descriptor descriptor;

	/**
	 * Constructor.
	 * @param window			Window handle
	 * @param lib				GLFW API
	 * @param descriptor		Window descriptor
	 */
	Window(Pointer window, DesktopLibrary lib, Descriptor descriptor) {
		this.handle = new Handle(window);
		this.lib = notNull(lib);
		this.descriptor = notNull(descriptor);
	}

	@Override
	public Handle handle() {
		return handle;
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
	public Device keyboard() {
		return new KeyboardDevice(this);
	}

	/**
	 * @return New mouse device
	 */
	public Device mouse() {
		return new MouseDevice(this);
	}

	/**
	 * Creates a Vulkan rendering surface for this window.
	 * @param vulkan Vulkan instance handle
	 * @return Vulkan surface
	 */
	public Handle surface(Handle vulkan) {
		final PointerByReference ref = new PointerByReference();
		final int result = lib.glfwCreateWindowSurface(vulkan, handle, null, ref);
		if(result != 0) {
			throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
		}
		return new Handle(ref.getValue());
	}

	@Override
	public void destroy() {
		lib.glfwDestroyWindow(handle);
	}
}
