package org.sarge.jove.material;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.input.Action;
import org.sarge.jove.input.InputEvent;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Adapter for a toggled effect.
 * @author Sarge
 */
public class ToggleProperty implements RenderProperty, Action {
	private final RenderProperty property;

	private boolean active = true;

	/**
	 * Constructor.
	 * @param property Delegate property
	 */
	public ToggleProperty( RenderProperty property ) {
		Check.notNull( property );
		this.property = property;
	}

	@Override
	public String getName() {
		return property.getType();
	}

	@Override
	public String getType() {
		return property.getType();
	}

	/**
	 * @return Whether this effect is active
	 */
	public boolean isActive() {
		return active;
	}

	/**
	 * Sets whether this effect is active
	 * @param active Active flag
	 */
	public void setActive( boolean active ) {
		this.active = active;
	}

	@Override
	public void apply( RenderingSystem sys ) {
		if( active ) property.apply( sys );
	}

	@Override
	public void reset( RenderingSystem sys ) {
		if( active ) property.reset( sys );
	}

	@Override
	public void execute( InputEvent event ) {
		active = !active;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
