package org.sarge.jove.input;

import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.HashCodeBuilder;

/**
 * Identifier for input events from {@link Device}s.
 * @author Sarge
 */
public class EventName {
	private final EventType type;
	private final String name;

	/**
	 * Constructor.
	 * @param type Event type
	 * @param name Event name, e.g. a keyboard key-name (optional)
	 */
	public EventName( EventType type, String name ) {
		Check.notNull( type );

		// Check name
		if( name != null ) {
			if( name.length() == 0 ) throw new IllegalArgumentException( "Event name cannot be empty" );
			if( name.indexOf( ' ' ) != -1 ) throw new IllegalArgumentException( "Event name cannot contain spaces: " + name );
		}

		// Check required name
		if( ( name == null ) != type.hasName() ) throw new IllegalArgumentException( "Event requires name: " + type );

		this.type = type;
		this.name = name;
	}

	public EventType getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	@Override
	public boolean equals( Object obj ) {
		return EqualsBuilder.equals( this, obj );
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.hashCode( this );
	}

	@Override
	public String toString() {
		if( name == null ) {
			return type.name();
		}
		else {
			return type.name() + "+" + name;
		}
	}
}
