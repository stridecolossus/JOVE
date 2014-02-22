package org.sarge.jove.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.input.InputEvent;
import org.sarge.jove.widget.ButtonWidget.State;
import org.sarge.jove.widget.RadioButtonWidget.ButtonGroup;

public class RadioButtonWidgetTest {
	private RadioButtonWidget radio;
	private ButtonGroup group;
	
	@Before
	public void before() {
		final Map<State, Drawable> states = new HashMap<State, Drawable>();
		states.put( State.NORMAL, mock( Drawable.class ) );
		group = new ButtonGroup( states );
		radio = new RadioButtonWidget( group, null );
	}
	
	@Test
	public void constructor() {
		assertEquals( group, radio.getButtonGroup() );
		assertNotNull( group.getButtons() );
		assertEquals( 1, group.getButtons().size() );
		assertEquals( radio, group.getButtons().iterator().next() );
		assertEquals( null, group.getSelectedButton() );
	}
	
	@Test
	public void handle() {
		// Select button
		radio.handle( mock( InputEvent.class ) );
		assertTrue( radio.isSelected() );
		assertEquals( radio, group.getSelectedButton() );
		
		// Add another button, select it and check first button is de-selected
		final RadioButtonWidget other = new RadioButtonWidget( group, null );
		other.handle( mock( InputEvent.class ) );
		assertTrue( other.isSelected() );
		assertFalse( radio.isSelected() );
		assertEquals( other, group.getSelectedButton() );
	}
	
	@Test
	public void add() {
		final RadioButtonWidget other = new RadioButtonWidget( group, null );
		assertEquals( 2, group.getButtons().size() );
		assertTrue( group.getButtons().contains( radio ) );
		assertTrue( group.getButtons().contains( other ) );
	}
}
