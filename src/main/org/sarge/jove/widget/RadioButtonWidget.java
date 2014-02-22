package org.sarge.jove.widget;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.sarge.jove.input.Action;
import org.sarge.jove.input.InputEvent;
import org.sarge.util.ToString;

/**
 * Radio button.
 * @author Sarge
 */
public class RadioButtonWidget extends ButtonWidget {
	/**
	 * Group of radio buttons.
	 */
	public static class ButtonGroup {
		private final Map<State, Drawable> states;
		private final Set<RadioButtonWidget> buttons = new HashSet<RadioButtonWidget>(); // TODO - strict
		
		private RadioButtonWidget selected;

		/**
		 * Constructor.
		 * @param states Button states
		 * TODO - check for NORMAL and SELECTED states?
		 */
		public ButtonGroup( Map<State, Drawable> states ) {
			this.states = Collections.unmodifiableMap( states );
		}
		
		/**
		 * @return Buttons in this group
		 */
		public Set<RadioButtonWidget> getButtons() {
			return new HashSet<RadioButtonWidget>( buttons );
		}
		
		/**
		 * @return Selected button in this group or <tt>null</tt> if none
		 */
		public RadioButtonWidget getSelectedButton() {
			return selected;
		}
		
		@Override
		public String toString() {
			return ToString.toString( this );
		}
	}
	
	private final ButtonGroup group;
	
	/**
	 * Constructor.
	 * @param group		Button group
	 * @param action	Optional action
	 */
	public RadioButtonWidget( ButtonGroup group, Action action ) {
		super( group.states, action );
		this.group = group;
		group.buttons.add( this );
	}
	
	/**
	 * @return Group for this radio button
	 */
	public ButtonGroup getButtonGroup() {
		return group;
	}
	
	@Override
	protected boolean handle( InputEvent e ) {
		// Delegate
		final boolean handled = super.handle( e );
		
		if( handled ) {
			// De-select other buttons in this group
			for( RadioButtonWidget b : group.buttons ) {
				b.setSelected( false );
			}

			// Select this radio button
			this.setSelected( true );
			group.selected = this;
		}
		
		return handled;
	}
}
