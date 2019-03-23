package org.sarge.jove.platform.glfw;

import static java.util.stream.Collectors.toList;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.sarge.jove.control.Event;
import org.sarge.jove.util.PowerSet;

/**
 * Helper methods for GLFW.
 * @author Sarge
 */
final class FrameworkHelper {
	private static final List<Set<Event.Modifier>> MODIFIERS = PowerSet.power(Arrays.asList(Event.Modifier.values())).collect(toList());

	private FrameworkHelper() {
	}

	/**
	 * Maps a GLFW action to an event type.
	 * @param action Action
	 * @return Event type
	 * @throws IllegalArgumentException if the action is not supported
	 */
	static Event.Type action(int action) {
		switch(action) {
		case 0: return Event.Type.RELEASE;
		case 1: return Event.Type.PRESS;
		case 2: return Event.Type.DOUBLE; // TODO
		default: throw new IllegalArgumentException("Unknown GLFW action: " + action);
		}
	}

	/**
	 * Maps a GLFW modifier bit-field to the corresponding set of event modifiers.
	 * @param mods Modifiers bit-field
	 * @return Modifiers
	 * @throws IndexOutOfBoundsException if the modifier bit-field is not valid
	 */
	static Set<Event.Modifier> modifiers(int mods) {
		return MODIFIERS.get(mods);
	}
}
