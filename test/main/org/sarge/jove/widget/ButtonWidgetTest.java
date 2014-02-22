package org.sarge.jove.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.input.Action;
import org.sarge.jove.input.InputEvent;
import org.sarge.jove.widget.ButtonWidget.State;

public class ButtonWidgetTest {
	private ButtonWidget button;
	private Drawable normal, selected;
	private Action action;
	
	@Before
	public void before() {
		// Create some states
		normal = mock( Drawable.class );
		selected = mock( Drawable.class );
		
		// Build states map
		final Map<State, Drawable> states = new HashMap<State, Drawable>();
		states.put( State.NORMAL, normal );
		states.put( State.SELECTED, selected );
		
		// Create button with an action
		action = mock( Action.class );
		button = new ButtonWidget( states, action );
	}
	
	@Test
	public void constructor() {
		assertFalse( button.isSelected() );
		assertTrue( button.isEnabled() );
		assertEquals( State.NORMAL, button.getState() );
	}
	
	@Test
	public void setSelected() {
		button.setSelected( true );
		assertTrue( button.isSelected() );
		assertEquals( State.SELECTED, button.getState() );
	}
	
	@Test
	public void setEnabled() {
		button.setSelected( true );
		button.setEnabled( false );
		assertFalse( button.isEnabled() );
		assertEquals( State.DISABLED, button.getState() );
	}
	
	@Test
	public void getDimensions() {
		// Check dimensions initially same as normal state
		when( normal.getDimensions() ).thenReturn( new Dimensions( 1, 1 ) );
		assertEquals( new Dimensions( 1, 1 ), button.getDimensions() );
		
		// Change state and check dimensions changed
		button.setSelected( true );
		when( selected.getDimensions() ).thenReturn( new Dimensions( 2, 2 ) );
		assertEquals( new Dimensions( 2, 2 ), button.getDimensions() );
	}
	
	@Test
	public void render() {
		// Render normal state
		button.render( null );
		verify( normal ).render( null );
		
		// Render selected state
		button.setSelected( true );
		button.render( null );
		verify( selected ).render( null );
		
		// Disable and check renders using default state since none specified
		button.setEnabled( false );
		button.render( null );
		verify( normal, times( 2 ) ).render( null );
	}
	
	@Test
	public void handle() {
		// Click and check selected
		final InputEvent event = mock( InputEvent.class );
		assertEquals( true, button.handle( event ) );
		assertTrue( button.isSelected() );
		
		// Click again and check de-selected
		assertEquals( true, button.handle( event ) );
		assertFalse( button.isSelected() );
		
		// Check action invoked
		verify( action, times( 2 ) ).execute( null );
		
		// Disable and check clicks are ignored
		button.setEnabled( false );
		assertEquals( false, button.handle( event ) );
		verifyNoMoreInteractions( action );
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void insufficientStates() {
		new ButtonWidget( new HashMap<State, Drawable>(), null );
	}
}
