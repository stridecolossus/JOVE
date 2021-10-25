package org.sarge.jove.control;

import org.sarge.jove.control.Event.Source;
import org.sarge.jove.control.Event.Type;

/**
 * A <i>button event</i> describes a toggle input such as keyboard keys or controller buttons.
 * @author Sarge
 */
@SuppressWarnings("unused")
public record Button(String name, Source source) implements Type<Button>, Event {
	@Override
	public Source source() {
		return source;
	}

	@Override
	public Type<?> type() {
		return this;
	}
}

// TODO
// - mods & action as args
// - name() generates on demand
// - also use + delimiter?
// - i.e. only care about name when persisting or displaying to user
// => less processing
// => general button & GLFW implementation with mods/action?
