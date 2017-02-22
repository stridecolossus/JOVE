package org.sarge.jove.input;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.HashCodeBuilder;

/**
 * Identifier for input events from {@link Device}s.
 * @author Sarge
 */
public final class EventKey {
	private final EventType type;
	private final String name;

	/**
	 * Constructor.
	 * @param type Event type
	 * @param name Event identifier
	 */
	public EventKey(EventType type, String name) {
		if(!type.hasName()) throw new IllegalArgumentException("Event does not require a name: " + type);
		Check.notEmpty(name);
		if(name.indexOf(' ') != -1) throw new IllegalArgumentException("Event name cannot contain spaces: " + name);
		this.type = type;
		this.name = name;
	}

	/**
	 * Constructor for an anonymous event.
	 * @param type Event type
	 */
	public EventKey(EventType type) {
		if(type.hasName()) throw new IllegalArgumentException("Event requires a name: " + type);
		this.type = type;
		this.name = null;
	}
	
	/**
	 * @return Type of event
	 */
	public EventType getType() {
		return type;
	}

	/**
	 * @return Event identifier
	 */
	public String getName() {
		return name;
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.equals(this, obj);
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.hashCode(this);
	}

	@Override
	public String toString() {
		if(name == null) {
			return type.name();
		}
		else {
			return type.name() + "+" + name;
		}
	}
}
