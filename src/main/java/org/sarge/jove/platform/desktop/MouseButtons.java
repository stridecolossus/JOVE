package org.sarge.jove.platform.desktop;

import java.awt.MouseInfo;
import java.lang.foreign.MemorySegment;
import java.util.function.*;
import java.util.stream.IntStream;

import org.sarge.jove.control.Button;
import org.sarge.jove.control.Button.*;
import org.sarge.jove.platform.desktop.DeviceLibrary.MouseButtonListener;

/**
 * The <i>mouse buttons</i> device generates button events from the mouse.
 * @author Sarge
 */
public class MouseButtons extends AbstractWindowDevice<ButtonEvent, MouseButtonListener> {
	private static final Button[] BUTTONS = IntStream
			.range(0, 16)
			.mapToObj(index -> new Button(index, String.format("Mouse-%d", index)))
			.toArray(Button[]::new);

	/**
	 * Constructor.
	 * @param window Parent window
	 */
	public MouseButtons(Window window) {
		super(window);
	}

	/**
	 * @return Number of mouse buttons
	 */
	public static int number() {
		return MouseInfo.getNumberOfButtons();
	}

	@Override
	protected MouseButtonListener callback(Window window, Consumer<ButtonEvent> listener) {
		return new MouseButtonListener() {
			@Override
			public void button(MemorySegment window, int button, int action, int mods) {
				final var event = new ButtonEvent(
						BUTTONS[button],
						ButtonAction.map(action),
						ModifierKey.map(mods)
				);
				listener.accept(event);
			}
		};
	}

	@Override
	protected BiConsumer<Window, MouseButtonListener> method(DeviceLibrary library) {
		return library::glfwSetMouseButtonCallback;
	}
}
