package org.sarge.jove.widget;

import java.util.Collections;
import java.util.Map;

import org.sarge.jove.input.Action;
import org.sarge.jove.input.InputEvent;

/**
 * Button widget.
 * @author Sarge
 * TODO
 * - highlighting
 */
public class ButtonWidget extends Widget {
	/**
	 * Button states.
	 */
	public static enum State {
		NORMAL,
		SELECTED,
		HIGHLIGHT_NORMAL,
		HIGHLIGHT_SELECTED,
		DISABLED,
	}
	
	private final Map<State, Drawable> states;
	private final Action action;
	
	private boolean selected;
	private boolean enabled = true;

	/**
	 * Constructor.
	 * @param states Button states
	 */
	public ButtonWidget( Map<State, Drawable> states, Action action ) {
		Check.notNull( states );
		if( !states.containsKey( State.NORMAL ) ) throw new IllegalArgumentException( "Button must have at least a NORMAL state drawable" );
		// TODO - enforce SELECTED for check-box and radio
		
		this.states = Collections.unmodifiableMap( states );
		this.action = action;
	}

	/**
	 * @return Whether this button is selected
	 */
	public boolean isSelected() {
		return selected;
	}
	
	/**
	 * @return Whether this button is enabled
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * Sets whether this button is enabled.
	 * @param enabled Enabled state
	 */
	public void setEnabled( boolean enabled ) {
		this.enabled = enabled;
	}

	/**
	 * Sets whether this button is selected.
	 * @param selected Selected state
	 */
	public void setSelected( boolean selected ) {
		this.selected = selected;
	}

	/**
	 * @return Button state
	 */
	protected State getState() {
		if( enabled ) {
			if( selected ) {
				return State.SELECTED;
			}
			else {
				return State.NORMAL;
			}
		}
		else {
			return State.DISABLED;
		}
	}
	
	@Override
	public Dimensions getDimensions() {
		final State state = getState();
		return states.get( state ).getDimensions();
	}
	
	@Override
	public void render( Object obj ) {
		// Lookup drawable for this state
		final State state = getState();
		Drawable d = states.get( state );
		
		// Use normal if none specified
		if( d == null ) d = states.get( State.NORMAL );

		// Render button
		d.render( null );
	}
	
	@Override
	protected boolean handle( InputEvent e ) {
		// Ignore if disabled
		if( !enabled ) return false;
		
		// Toggle state
		selected = !selected;
		
		// Delegate to handler
		if( action != null ) {
			// TODO - need to differentiate between press/release?
			action.execute( null );
		}
		
		// Notify event has been consumed by this button
		return true;
	}
}
