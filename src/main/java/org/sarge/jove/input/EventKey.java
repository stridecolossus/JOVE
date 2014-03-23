package org.sarge.jove.input;

import org.sarge.jove.util.DefaultObjectPool;
import org.sarge.jove.util.ObjectPool;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.HashCodeBuilder;

/**
 * Identifier for input events from {@link Device}s.
 * @author Sarge
 */
public class EventKey {
	/**
	 * Unlimited object pool for input keys.
	 */
	public static final ObjectPool<EventKey> POOL = new DefaultObjectPool<EventKey>() {
		@Override
		protected EventKey create() {
			return new EventKey();
		}
	};

	private EventType type;
	private String name;

	/**
	 * Default constructor for pooled event key.
	 * @see #init(EventType, String)
	 */
	private EventKey() {
	}

	/**
	 * Constructor.
	 * @param type Event type
	 * @param name Event identifier
	 */
	public EventKey( EventType type, String name ) {
		init( type, name );
	}

	/**
	 * Initialiser.
	 * @param type Event type
	 * @param name Event name, e.g. a keyboard key-name (optional)
	 */
	@SuppressWarnings("hiding")
	protected EventKey init( EventType type, String name ) {
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

		return this;
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
