package org.sarge.jove.control;

import org.sarge.jove.control.Event.AbstractEvent;

/**
 * A <i>button event</i> describes a toggle input such as keyboard keys or controller buttons.
 * @author Sarge
 */
public class ButtonEvent extends AbstractEvent {
	/**
	 * Constructor.
	 * @param name		Button identifier
	 * @param type		Button type
	 * @param src		Event source
	 */
	public ButtonEvent(String name, Type type, Source src) {
		super(name, type, src);
	}
}
