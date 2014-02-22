package org.sarge.jove.widget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Location;

public class ListWidgetLayoutTest {
	private ListWidgetLayout layout;
	
	@Before
	public void before() {
		layout = new ListWidgetLayout( true );
	}
	
	@Test
	public void apply() {
		// Create a widget to be laid out
		final Widget widget = mock( Widget.class );
		when( widget.isVisible() ).thenReturn( true );
		when( widget.getDimensions() ).thenReturn( new Dimensions( 3, 4 ) );
		
		// Apply and check container dimensions match widget
		final Dimensions dim = layout.apply( new Location( 1, 2 ), Collections.singletonList( widget ) );
		assertEquals( new Dimensions( 3, 4 ), dim );
		
		// Check widget position set relative to layout origin
		verify( widget ).setLocation( new Location( 1, 2 + 4 ) );			// Note - incorporates step down!
	}
}
