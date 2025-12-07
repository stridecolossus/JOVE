package org.sarge.jove.control;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.Consumer;

import org.sarge.jove.control.Button.ButtonEvent;
import org.sarge.jove.foreign.Callback;

/**
 * A <i>device button adapter</i> is a multiplexer for a set of button event listeners.
 * TODO
 * @author Sarge
 */
public class ButtonDeviceAdapter {
	private final Device<ButtonEvent> device;
	private final Map<Integer, Consumer<ButtonEvent>> handlers = new HashMap<>();
	private Callback callback;

	/**
	 * Constructor.
	 * @param device Parent device
	 */
	public ButtonDeviceAdapter(Device<ButtonEvent> device) {
		this.device = requireNonNull(device);
	}

	/**
	 * Creates a device for the given button.
	 * @param index Button index
	 * @return Button device
	 */
	public Device<ButtonEvent> button(int index) {
		return new Device<>() {
			@Override
			public Callback bind(Consumer<ButtonEvent> listener) {
				// Validate
				if(handlers.containsKey(index)) {
					throw new IllegalStateException("Button already bound: " + index);
				}

				// Bind the parent device as required
				if(callback == null) {
					callback = device.bind(this::handle);
				}

				// Register button handler
				handlers.put(index, listener);

				return callback;
			}

			@Override
			public void remove() {
				// Remove handler
				final var handler = handlers.remove(index);
				if(handler == null) {
					throw new IllegalStateException();
				}

				// Remove the parent device binding if no buttons
				if(handlers.isEmpty()) {
					device.remove();
					callback = null;
				}
			}

			/**
			 *
			 * @param event
			 */
			private void handle(ButtonEvent event) {
				final Consumer<ButtonEvent> handler = handlers.get(event.button().index());
				if(handler != null) {
					handler.accept(event);
				}
			}
		};
	}
}
