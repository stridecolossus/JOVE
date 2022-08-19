package org.sarge.jove.control;

import java.util.*;
import java.util.function.Consumer;

import org.sarge.jove.control.Event.Source;

public interface Action<E extends Event> {

	String name();

	void execute(E event);

	class Bindings implements Consumer<Event> {
		private final Map<Object, Runnable> handlers = new HashMap<>();
		private final Map<Action<?>, Set<Runnable>> bindings = new HashMap<>();

		public <E extends Event> void bind(Source<E> source, Action<E> action) {

		}

		public void bind(Axis axis, Action<Axis> action) {
			final Runnable handler = () -> action.execute(axis);
			handlers.put(axis.type(), handler);
		}

		public <T> void bind(Button<T> button, Action<Button<T>> action) {
			final Runnable handler = () -> action.execute(button);
			handlers.put(button.type(), handler);
		}

		public void remove(Action<?> action) {

			for(Runnable r : bindings.get(action)) {
				handlers.remove(r);
			}

			bindings.remove(action);
		}

		@Override
		public void accept(Event event) {
			// Lookup action
			final Runnable handler = handlers.get(event.type());

			// Ignore if not bound
			if(handler == null) {
				return;
			}

			// TODO - match buttons

			// Delegate to action
			handler.run();
		}
	}
}
