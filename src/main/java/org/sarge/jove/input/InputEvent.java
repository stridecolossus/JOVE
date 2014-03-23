package org.sarge.jove.input;

import org.sarge.jove.common.Location;
import org.sarge.jove.util.DefaultObjectPool;
import org.sarge.jove.util.ObjectPool;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.ToString;

/**
 * Input event.
 * @author Sarge
 */
public class InputEvent {
	/**
	 * Pool for input events.
	 */
	public static final ObjectPool<InputEvent> POOL = new DefaultObjectPool<InputEvent>() {
		@Override
		protected InputEvent create() {
			return new InputEvent();
		}
	};

	private Device dev;
	private EventKey key;
	private Location loc;
	private Integer zoom;

	/**
	 * Pool constructor.
	 */
	private InputEvent() {
	}

	/**
	 * Constructor.
	 * @param dev		Device
	 * @param key		Event descriptor
	 * @param loc		Event location
	 * @param zoom		Zoom value
	 */
	public InputEvent( Device dev, EventKey key, Location loc, Integer zoom ) {
		init( dev, key );
		if( loc != null ) setLocation( loc );
		if( zoom != null ) setZoom( zoom );
	}

	/**
	 * Re-initialises this event.
	 * @param dev Source device
	 * @param key Event key
	 */
	@SuppressWarnings("hiding")
	protected void init( Device dev, EventKey key ) {
		Check.notNull( dev );
		Check.notNull( key );

		this.dev = dev;
		this.key = key;
		this.loc = null;
		this.zoom = null;
	}

	/**
	 * @return Source device
	 */
	public Device getDevice() {
		return dev;
	}

	/**
	 * @return Event key
	 */
	public EventKey getEventKey() {
		return key;
	}

	/**
	 * @return Screen/drag coordinates
	 */
	public Location getLocation() {
		return loc;
	}

	/**
	 * Sets the location argument.
	 * @param loc Event location
	 * @throws IllegalArgumentException if this event does not require a location
	 */
	protected void setLocation( Location loc ) {
		if( !key.getType().hasLocation() ) throw new IllegalArgumentException( "Location invalid for " + key.getType() );
		this.loc = loc;
	}

	/**
	 * @return Zoom value
	 */
	public Integer getZoom() {
		return zoom;
	}

	/**
	 * Sets the zoom argument.
	 * @param zoom Zoom value
	 * @throws IllegalArgumentException if this event is not {@link EventType#ZOOM}
	 */
	protected void setZoom( Integer zoom ) {
		if( key.getType() != EventType.ZOOM ) throw new IllegalArgumentException( "Zoom invalid for " + key.getType() );
		this.zoom = zoom;
	}

	@Override
	public boolean equals( Object obj ) {
		return EqualsBuilder.equals( this, obj );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
