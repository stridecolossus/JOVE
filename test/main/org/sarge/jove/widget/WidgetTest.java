package org.sarge.jove.widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Location;
import org.sarge.jove.input.InputEvent;

public class WidgetTest {
	class MockWidget extends Widget {
		@Override
		public Dimensions getDimensions() {
			return new Dimensions( 3, 4 );
		}
		
		@Override
		protected void render( Object obj ) {
			// Unused
		}
	}
	
	private MockWidget widget;
	
	@Before
	public void before() {
		widget = new MockWidget();
	}
	
	@Test
	public void constructor() {
		assertEquals( new Location(), widget.getLocation() );
		assertEquals( null, widget.getParent() );
		assertNotNull( widget.getChildren() );
		assertTrue( widget.getChildren().isEmpty() );
		assertEquals( null, widget.getTooltip() );
		assertEquals( WidgetAlignment.CENTRE, widget.getHorizontalAlignment() );
		assertEquals( WidgetAlignment.CENTRE, widget.getVerticalAlignment() );
		assertTrue( widget.isVisible() );
	}
	
	@Test
	public void setLocation() {
		final Location loc = new Location( 1, 2 );
		widget.setLocation( loc );
		assertEquals( loc, widget.getLocation() );
	}
	
	@Test
	public void contains() {
		assertTrue( widget.contains( new Location( 0, 0 ) ) );
		assertTrue( widget.contains( new Location( 3, 4 ) ) );
		assertFalse( widget.contains( new Location( 8, 9 ) ) );
	}
	
	@Test
	public void setAlignment() {
		widget.setHorizontalAlignment( WidgetAlignment.MIN );
		widget.setVerticalAlignment( WidgetAlignment.MAX );
		assertEquals( WidgetAlignment.MIN, widget.getHorizontalAlignment() );
		assertEquals( WidgetAlignment.MAX, widget.getVerticalAlignment() );
	}
	
	@Test
	public void setVisible() {
		widget.setVisible( false );
		assertFalse( widget.isVisible() );
	}
	
	@Test
	public void setTooltip() {
		widget.setTooltip( "tooltip" );
		assertEquals( "tooltip", widget.getTooltip() );
	}
	
	@Test
	public void handle() {
		assertFalse( widget.handle( mock( InputEvent.class ) ) );
	}
}
