package org.sarge.jove.platform.desktop;

import static org.sarge.jove.util.Check.notNull;

import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.control.Button;
import org.sarge.jove.control.Device;
import org.sarge.jove.control.InputEvent.Handler;
import org.sarge.jove.control.InputEvent.Type;
import org.sarge.jove.platform.desktop.DesktopLibraryDevice.KeyListener;

/**
 * A <i>keyboard device</i> generates {@link Button} events.
 * @author Sarge
 */
class KeyboardDevice implements Device {
	private final Window window;

	/**
	 * Constructor.
	 * @param window Window
	 */
	KeyboardDevice(Window window) {
		this.window = notNull(window);
	}

	@Override
	public String name() {
		return "Keyboard";
	}

	@Override
	public Set<Class<? extends Type>> types() {
		return Set.of(Button.class);
	}

	@Override
	public void enable(Class<? extends Type> type, Handler handler) {
//		Check.notNull(handler);

		// Create callback adapter
		final KeyListener listener = (ptr, key, scancode, action, mods) -> {
//			final Button button = new Button(key, action, mods);
//			handler.handle(button.event());
			System.out.println("key="+key+" action="+action+" mods="+mods);
			if(key == 256) System.exit(0);
		};

		// Register callback
		apply(type, listener);
	}

	@Override
	public void disable(Class<? extends Type> type) {
		apply(type, null);
	}

	private void apply(Class<? extends Type> type, KeyListener listener) {
		if(type != Button.class) throw new IllegalArgumentException("Invalid event type for keyboard: " + type);
		window.library().glfwSetKeyCallback(window.handle(), listener);
	}

	///////////////
	public static void main(String[] args) throws InterruptedException {
		Desktop desktop = Desktop.create();
		WindowDescriptor descriptor = new WindowDescriptor.Builder().title("test").size(new Dimensions(640, 480)).build();
		Window window = desktop.window(descriptor);
		window.keyboard().enable(Button.class, null);
		while(true) {
			Thread.sleep(50);
			desktop.poll();
		}
	}
}
