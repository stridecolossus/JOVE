package org.sarge.jove.input;

import org.sarge.jove.common.Location;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.EqualsBuilder;
import org.sarge.lib.util.HashCodeBuilder;
import org.sarge.lib.util.ToString;

/**
 * Input event.
 * @author Sarge
 */
public final class InputEvent {
	private final Device dev;
	private final EventKey key;
	private Location loc;
	private Integer zoom;

	/**
	 * Constructor.
	 * @param dev		Device
	 * @param key		Event descriptor
	 */
	public InputEvent(Device dev, EventKey key) {
		Check.notNull(dev);
		Check.notNull(key);
		this.dev = dev;
		this.key = key;
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
	public void setLocation(Location loc) {
		if(!key.getType().hasLocation()) throw new IllegalArgumentException("Location invalid for " + key.getType());
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
	public void setZoom(Integer zoom) {
		if(key.getType() != EventType.ZOOM) throw new IllegalArgumentException("Zoom invalid for " + key.getType());
		this.zoom = zoom;
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
		return ToString.toString(this);
	}
}
