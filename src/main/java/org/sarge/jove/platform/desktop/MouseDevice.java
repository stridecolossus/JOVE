package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notNull;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.control.Device;
import org.sarge.jove.control.InputEvent.Handler;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.control.InputEvent.Type.Axis;
import org.sarge.jove.control.InputEvent.Type.Button;
import org.sarge.jove.control.InputEvent.Type.Position;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseButtonListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MousePositionListener;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.MouseScrollListener;

import com.sun.jna.Callback;

/**
 * A <i>mouse device</i> generates events for a mouse controller.
 * <p>
 * Mouse buttons generate {@link Button}, moving the mouse generates {@link Position} and the mouse wheel maps to an {@link Axis}.
 * <p>
 * @author Sarge
 */
class MouseDevice implements Device {
	private static final Axis WHEEL = new Axis(0, "Wheel");

	/**
	 * Mouse callback entry.
	 * @param <T> Mouse callback type
	 */
	private class Entry<T extends Callback> {
		private final Function<Handler, T> mapper;
		private final BiConsumer<Handle, T> setter;

		/**
		 * Constructor.
		 * @param mapper Maps the event handler adapter to the callback
		 * @param setter Applies the callback
		 */
		private Entry(Function<Handler, T> mapper, BiConsumer<Handle, T> setter) {
			this.mapper = notNull(mapper);
			this.setter = notNull(setter);
		}

		/**
		 * Applies this callback entry.
		 * @param handler Event handler
		 */
		private void apply(Handler handler) {
			final T callback = mapper.apply(handler);
			setter.accept(window.handle(), callback);
		}
	}

	private final Window window;
	private final Map<Class<? extends Type>, Entry<?>> map;

	/**
	 * Builds the callback entries indexed by event type.
	 */
	private Map<Class<? extends Type>, Entry<?>> build() {
		final var lib = window.library();
		return Map.of(
			Button.class, 		new Entry<>(this::button, 	lib::glfwSetMouseButtonCallback),
			Position.class,		new Entry<>(this::position, lib::glfwSetCursorPosCallback),
			Axis.class,			new Entry<>(this::wheel, 	lib::glfwSetScrollCallback)
		);
	}
	// TODO - can we make this static rather than per-window?

	/**
	 * Constructor.
	 * @param window Window
	 */
	MouseDevice(Window window) {
		this.window = notNull(window);
		this.map = build();
	}

	@Override
	public String name() {
		return "Mouse";
	}

	@Override
	public Set<Class<? extends Type>> types() {
		return map.keySet();
	}

	@Override
	public void enable(Class<? extends Type> type, Handler handler) {
		final Entry<?> entry = entry(type);
		entry.apply(handler);
	}

	@Override
	public void disable(Class<? extends Type> type) {
		final Entry<?> entry = entry(type);
		entry.setter.accept(window.handle(), null);
	}

	private Entry<?> entry(Class<? extends Type> type) {
		final Entry<?> entry = map.get(type);
		if(entry == null) throw new IllegalArgumentException("Invalid event type for mouse: " + type);
		return entry;
	}

	// TODO
	// - make these static/constants?

	/**
	 *
	 * @param handler
	 * @return
	 */
	protected MouseButtonListener button(Handler handler) {
		return (ptr, id, action, mods) -> {
			// TODO - action/mods
			final Button button = new Button(id, String.valueOf(id), action, mods);
			handler.handle(button.event());
		};
	}

	/**
	 *
	 * @param handler
	 * @return
	 */
	protected MousePositionListener position(Handler handler) {
		return (ptr, x, y) -> handler.handle(Position.POSITION.create((float) x, (float) y));
	}

	/**
	 *
	 * @param handler
	 * @return
	 */
	protected MouseScrollListener wheel(Handler handler) {
		return (ptr, x, y) -> handler.handle(WHEEL.create((float) y));
	}
}
