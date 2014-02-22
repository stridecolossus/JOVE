package org.sarge.jove.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Location;
import org.sarge.jove.input.InputEvent;

public class ContainerWidgetTest {
	private ContainerWidget container;
	private WidgetLayout layout;
	private Widget widget;
	
	@Before
	public void before() {
		layout = mock( WidgetLayout.class );
		container = new ContainerWidget( layout );
		widget = mock( Widget.class );
	}
	
	@Test
	public void constructor() {
		assertEquals( layout, container.getLayout() );
		assertTrue( container.getChildren().isEmpty() );
		assertEquals( Background.TRANSPARENT, container.getBackground() );
		verify( layout ).apply( container.getLocation(), container.getChildren() );
	}
	
	@Test
	public void setBackground() {
		final Background back = mock( Background.class );
		container.setBackground( back );
		assertEquals( back, container.getBackground() );
	}
	
	@Test
	public void setLayout() {
		// Create a new layout
		final WidgetLayout other = mock( WidgetLayout.class );
		final Dimensions dim = new Dimensions( 1, 2 );
		when( other.apply( container.getLocation(), container.getChildren() ) ).thenReturn( dim );
		
		// Set layout and check applied to contents
		container.setLayout( other );
		assertEquals( other, container.getLayout() );
		verify( other ).apply( container.getLocation(), container.getChildren() );
		
		// Check container dimensions updated
		assertEquals( dim, container.getDimensions() );
	}
	
	@Test
	public void contents() {
		// Add a child and check layout updated
		container.add( widget );
		assertTrue( container.getChildren().contains( widget ) );
		verify( layout, times( 2 ) ).apply( container.getLocation(), Collections.singletonList( widget ) );

		// Remove it and check layout updated
		container.remove( widget );
		assertTrue( container.getChildren().isEmpty() );
		verify( layout, times( 3 ) ).apply( container.getLocation(), Collections.EMPTY_LIST );
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void addSelf() {
		container.add( container );
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void addDuplicate() {
		container.add( widget );
		container.add( widget );
	}
	
	@Test
	public void clear() {
		container.add( widget );
		container.clear();
		assertTrue( container.getChildren().isEmpty() );
		verify( layout, times( 3 ) ).apply( container.getLocation(), Collections.EMPTY_LIST );
	}
	
	@Test
	public void find() {
		// Set a widget at a location
		final Location loc = new Location( 1, 2 );
		container.add( widget );
		when( widget.contains( loc ) ).thenReturn( true );
		when( widget.isVisible() ).thenReturn( true );
		
		// Check widget is found at this location
		assertEquals( widget, container.find( loc ) );

		// Check not found at other locations
		assertEquals( null, container.find( new Location() ) );
		
		// Check not found when invisible
		when( widget.isVisible() ).thenReturn( true );
		assertEquals( widget, container.find( loc ) );
	}
	
	@Test
	public void render() {
		// Add a background
		final Background back = mock( Background.class );
		container.setBackground( back );
		
		// Add a child and render
		when( widget.isVisible() ).thenReturn( true );
		container.add( widget );
		container.render( null );
		
		// Check background rendered
		verify( back ).render();
		
		// Check child rendered
		verify( widget ).render( null );
		
		// Set child hidden and check not rendered
		when( widget.isVisible() ).thenReturn( false );
		container.render( null );
		verify( widget, times( 2 ) ).isVisible();
		verifyNoMoreInteractions( widget );
	}
	
	@Test
	public void handle() {
		// Check event is discarded if no contents
		final InputEvent event = mock( InputEvent.class );
		assertFalse( container.handle( event ) );
		
		// Add an invisible child and check still discarded
		container.add( widget );
		assertFalse( container.handle( event ) );
		
		// Make visible and check handled
		when( widget.isVisible() ).thenReturn( true );
		when( widget.handle( event ) ).thenReturn( true );
		assertTrue( container.handle( event ) );
		verify( widget ).handle( event );
	}
}
