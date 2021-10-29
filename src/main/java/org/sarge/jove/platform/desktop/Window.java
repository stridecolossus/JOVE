package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.LazySupplier;

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
		/**
		 * Constructor.
		 * @param title				Window title
		 * @param size				Dimensions
		 * @param properties		Properties
		 */
		public Descriptor {
			Check.notEmpty(title);
			Check.notNull(size);
			properties = Check.notNull(properties);
		}

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
				this.title = notEmpty(title);
				return this;
			}

			/**
			 * Sets the size of the window.
			 * @param size Window size
			 */
			public Builder size(Dimensions size) {
				this.size = notNull(size);
				return this;
			}

			/**
			 * Adds a window property.
			 * @param p Property
			 */
			public Builder property(Property p) {
				props.add(notNull(p));
				return this;
			}

			/**
			 * Constructs this descriptor.
			 * @return New window descriptor
			 */
			public Descriptor build() {
				return new Descriptor(title, size, props);
			}
		}
	}

	/**
	 * Creates a new window.
	 * @param desktop			Desktop service
	 * @param descriptor		Window descriptor
	 * @param monitor			Optional monitor
	 * @return New window
	 * @throws RuntimeException if the window cannot be created
	 */
	public static Window create(Desktop desktop, Descriptor descriptor, Monitor monitor) {
		// Apply window hints
		final DesktopLibrary lib = desktop.library();
		lib.glfwDefaultWindowHints();
		for(Property p : descriptor.properties) {
			p.apply(lib);
		}

		// Create window
		final Dimensions size = descriptor.size();
		final Pointer window = lib.glfwCreateWindow(size.width(), size.height(), descriptor.title(), null, null);	// TODO - monitor
		if(window == null) {
			throw new RuntimeException(String.format("Window cannot be created: descriptor=%s monitor=%s", descriptor, monitor));
		}

		// Create window wrapper
		return new Window(desktop, window, descriptor);
	}

	private final Desktop desktop;
	private final Descriptor descriptor;
	private final Supplier<KeyboardDevice> keyboard = new LazySupplier<>(() -> new KeyboardDevice(this));
	private final Supplier<MouseDevice> mouse = new LazySupplier<>(() -> new MouseDevice(this));

	/**
	 * Constructor.
	 * @param desktop			Desktop service
	 * @param window			Window handle
	 * @param descriptor		Window descriptor
	 */
	private Window(Desktop desktop, Pointer window, Descriptor descriptor) {
		super(new Handle(window));
		this.desktop = notNull(desktop);
		this.descriptor = notNull(descriptor);
	}

	/**
	 * @return Descriptor for this window
	 */
	public Descriptor descriptor() {
		return descriptor;
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
	 * Creates a Vulkan rendering surface for this window.
	 * @param instance Vulkan instance
	 * @return Vulkan surface
	 */
	public Handle surface(Handle instance) {
		final DesktopLibrary lib = desktop.library();
		final PointerByReference ref = new PointerByReference();
		final int result = lib.glfwCreateWindowSurface(instance, this, null, ref);
		if(result != 0) {
			throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
		}
		return new Handle(ref.getValue());
	}

	@Override
	protected void release() {
		final DesktopLibrary lib = desktop.library();
		lib.glfwDestroyWindow(this);
	}
}
