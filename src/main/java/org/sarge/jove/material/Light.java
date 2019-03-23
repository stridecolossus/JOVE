package org.sarge.jove.material;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.material.Material.Property;
import org.sarge.jove.material.Material.Property.Policy;

/**
 * A <i>light</i> is a model for a light-source.
 * @author Sarge
 */
public class Light {
	/**
	 * Creates an ambient light source.
	 * @param col Colour
	 * @return Ambient light
	 */
	public static Light ambient(Colour col) {
		return new Light(col, null, null);
	}

	/**
	 * Creates a directional light source.
	 * @param col Colour
	 * @param dir Direction
	 * @return Directional light
	 */
	public static Light directional(Colour col, Vector dir) {
		return new Light(col, null, dir);
	}

	/**
	 * Creates a point source.
	 * @param col Colour
	 * @param pos Position
	 * @return Point light
	 */
	public static Light point(Colour col, Point pos) {
		return new Light(col, pos, null);
	}

	/**
	 * Creates a spotlight.
	 * @param col Colour
	 * @param pos Position
	 * @param dir Direction
	 * @return Spotlight
	 */
	public static Light spotlight(Colour col, Point pos, Vector dir) {
		return new Light(col, pos, dir);
	}

	private Colour col;
	private Point pos;
	private Vector dir;

	/**
	 * Constructor.
	 * @param col Colour
	 * @param pos Position
	 * @param dir Direction
	 */
	protected Light(Colour col, Point pos, Vector dir) {
		this.col = notNull(col);
		this.pos = pos;
		this.dir = dir;
	}

	/**
	 * @return Light colour
	 */
	public Colour colour() {
		return col;
	}

	/**
	 * Sets the colour of this light.
	 * @param col Light colour
	 */
	public void colour(Colour col) {
		this.col = notNull(col);
	}

	/**
	 * @return Light position
	 */
	public Point position() {
		return pos;
	}

	/**
	 * Sets the position of this light.
	 * @param pos Light position
	 * @throws IllegalArgumentException if this light does not have a position
	 */
	public void position(Point pos) {
		if(this.pos == null) throw new IllegalArgumentException("Light does not have a position");
		this.pos = notNull(pos);
	}

	/**
	 * @return Light direction
	 */
	public Vector direction() {
		return dir;
	}

	/**
	 * Sets the direction of this light.
	 * @param dir Light direction
	 * @throws IllegalArgumentException if this light does not have a direction
	 */
	public void direction(Vector dir) {
		if(this.dir == null) throw new IllegalArgumentException("Light does not have a direction");
		this.dir = notNull(dir);
	}

	/**
	 * Builds the material properties for this light.
	 * @return Material properties
	 */
	public Collection<Property> properties() {
		// Add colour property
		final List<Property> props = new ArrayList<>(3);
		props.add(new Property(BufferPropertyBinder.colour(() -> col), Policy.MANUAL));

		// Add position property
		if(pos != null) {
			props.add(new Property(BufferPropertyBinder.tuple(() -> pos), Policy.NODE));
		}

		// Add direction property
		if(dir != null) {
			props.add(new Property(BufferPropertyBinder.tuple(() -> dir), Policy.FRAME));		// TODO - or per-node?
		}

		return props;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
