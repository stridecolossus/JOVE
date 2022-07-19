package org.sarge.jove.platform.desktop;

import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.function.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.control.WindowListener;
import org.sarge.lib.util.*;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

/**
 * Native window implemented using GLFW.
 * @author Sarge
 */
public class Window extends AbstractTransientNativeObject {
	/**
	 * Window hints.
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
		DISABLE_OPENGL(0x00022001);

		private final int hint;

		private Hint(int hint) {
			this.hint = hint;
		}

		/**
		 * Applies this hint.
		 * @param lib Desktop library
		 */
		void apply(DesktopLibrary lib) {
			final int value = this == DISABLE_OPENGL ? 0 : 1;
			lib.glfwWindowHint(hint, value);
		}
	}

	/**
	 * Descriptor for the properties of this window.
	 */
	public record Descriptor(String title, Dimensions size, Set<Hint> hints) {
		/**
		 * Constructor.
		 * @param title			Window title
		 * @param size			Dimensions
		 * @param hints			Window hints
		 */
		public Descriptor {
			Check.notEmpty(title);
			Check.notNull(size);
			hints = Check.notNull(hints);
		}
	}

	private final Desktop desktop;
	private final Descriptor descriptor;
	private final Supplier<KeyboardDevice> keyboard = new LazySupplier<>(() -> new KeyboardDevice(this));
	private final Supplier<MouseDevice> mouse = new LazySupplier<>(() -> new MouseDevice(this));
	private final Map<Object, Set<Callback>> registry = new WeakHashMap<>();

	/**
	 * Constructor.
	 * @param desktop			Desktop service
	 * @param window			Window handle
	 * @param descriptor		Window descriptor
	 */
	Window(Desktop desktop, Pointer window, Descriptor descriptor) {
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
	 * Sets the event listener for this window.
	 * @param listener Listener for window state changes or {@code null} to remove the listener
	 */
	public void listener(WindowListener listener) {
		final DesktopLibrary lib = desktop.library();
		if(listener == null) {
			lib.glfwSetCursorEnterCallback(this, null);
			lib.glfwSetWindowFocusCallback(this, null);
			lib.glfwSetWindowIconifyCallback(this, null);
			lib.glfwSetWindowSizeCallback(this, null);
		}
		else {
			listener(lib::glfwSetCursorEnterCallback, (window, enter) -> listener.cursor(enter));
			listener(lib::glfwSetWindowFocusCallback, (window, focus) -> listener.focus(focus));
			listener(lib::glfwSetWindowIconifyCallback, (window, iconify) -> listener.minimised(iconify));
			listener(lib::glfwSetWindowSizeCallback, (window, w, h) -> listener.resize(w, h));
		}
	}

	/**
	 * Helper - Registers a window listener.
	 * <p>
	 * Note that JNA callbacks <b>must</b> contain a single public method, i.e. the same {@link WindowListener} cannot be passed to each API method</li>
	 * <p>
	 * @param <T> Callback type
	 * @param method		GLFW registration method
	 * @param listener		Callback
	 */
	private <T extends Callback> void listener(BiConsumer<Window, T> method, T listener) {
		method.accept(this, listener);
		register("window.listeners", listener);
	}

	/**
	 * Registers a JNA callback listener attached to this window.
	 * <p>
	 * The set of listeners is <i>weakly</i> referenced by the given key.
	 * This prevents callbacks being garbage collected and thus de-registered by GLFW until the key itself becomes stale.
	 * <p>
	 * @param key			Key
	 * @param listener		Listener
	 */
	protected void register(Object key, Callback listener) {
		final Set<Callback> set = registry.computeIfAbsent(key, ignored -> new HashSet<>());
		set.add(notNull(listener));
	}

	/**
	 * Creates a Vulkan rendering surface for this window.
	 * @param instance Vulkan instance
	 * @return Vulkan surface
	 */
	public Handle surface(Handle instance) {
		final DesktopLibrary lib = desktop.library();
		final PointerByReference ref = desktop.factory().pointer();
		final int result = lib.glfwCreateWindowSurface(instance, this, null, ref);
		if(result != 0) {
			throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
		}
		return new Handle(ref.getValue());
	}

	@Override
	protected void release() {
		desktop.library().glfwDestroyWindow(this);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(descriptor)
				.build();
	}

	/**
	 * Builder for a window.
	 */
	public static class Builder {
		private String title;
		private Dimensions size;
		private final Set<Hint> hints = new HashSet<>();
		private Monitor monitor;

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
		 * Adds a window hint.
		 * @param hint Window hint
		 */
		public Builder hint(Hint hint) {
			hints.add(notNull(hint));
			return this;
		}

		/**
		 * Sets the monitor for this window.
		 * @param monitor Monitor
		 */
		public Builder monitor(Monitor monitor) {
			this.monitor = notNull(monitor);
			return this;
		}

		/**
		 * Constructs this window.
		 * @return New window
		 * @throws RuntimeException if the window cannot be created
		 */
		public Window build(Desktop desktop) {
			// Apply window hints
			final DesktopLibrary lib = desktop.library();
			lib.glfwDefaultWindowHints();
			for(Hint hint : hints) {
				hint.apply(lib);
			}

			// Create window descriptor
			final Descriptor descriptor = new Descriptor(title, size, hints);

			// Create window
			final Pointer window = lib.glfwCreateWindow(size.width(), size.height(), title, monitor, null);
			if(window == null) {
				throw new RuntimeException(String.format("Window cannot be created: descriptor=%s monitor=%s", descriptor, monitor));
			}

			// Create window domain object
			return new Window(desktop, window, descriptor);
		}
	}
}
