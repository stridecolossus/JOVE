package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notEmpty;
import static org.sarge.jove.util.Check.notNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.sarge.jove.common.Dimensions;

/**
 * Descriptor for a window.
 */
public final class WindowDescriptor {
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
	public WindowDescriptor(String title, Dimensions size, Monitor monitor, Set<Property> props) {
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

	/**
	 * @return Window properties
	 */
	public Set<Property> properties() {
		return props;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		else {
			return
					(obj instanceof WindowDescriptor that) &&
					this.title.equals(that.title) &&
					this.size.equals(that.size) &&
					this.monitor.equals(that.monitor) &&
					this.props.equals(that.props);
		}
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
		public Builder property(WindowDescriptor.Property p) {
			props.add(p);
			return this;
		}

		/**
		 * Constructs this descriptor.
		 * @param New descriptor
		 */
		public WindowDescriptor build() {
			return new WindowDescriptor(title, size, monitor, props);
		}
	}
}