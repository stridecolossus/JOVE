package org.sarge.jove.input;

import org.sarge.jove.common.Location;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.ToString;

/**
 * Input event.
 * @author Sarge
 */
public class InputEvent {
	private final Device dev;
	private final EventName key;
	private final Location loc;
	private final Integer zoom;

	/**
	 * Constructor.
	 * @param dev		Source device
	 * @param key		Event descriptor
	 * @param pos		Screen/drag coordinates or <tt>null</tt>
	 * @param zoom		Zoom or <tt>null</tt> if not zooming
	 */
	public InputEvent( Device dev, EventName key, Location loc, Integer zoom ) {
		Check.notNull( dev );
		Check.notNull( key );

		this.dev = dev;
		this.key = key;
		this.loc = loc;
		this.zoom = zoom;

		// Verify args
		switch( key.getType() ) {
		case ZOOM:
			if( zoom == null ) throw new IllegalArgumentException( "Event requires a zoom parameter: " + this );
			break;
		}
	}

	/**
	 * @return Source device
	 */
	public Device getDevice() {
		return dev;
	}

	/**
	 * @return Event descriptor
	 */
	public EventName getEventKey() {
		return key;
	}

	/**
	 * @return Screen/drag coordinates
	 */
	public Location getLocation() {
		return loc;
	}

	/**
	 * @return Zoom value
	 */
	public Integer getZoom() {
		return zoom;
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
