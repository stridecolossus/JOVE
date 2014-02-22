package org.sarge.jove.widget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.common.Location;

public class RootWidgetTest {
	private RootWidget root;
	
	@Before
	public void before() {
		root = new RootWidget( new Location( 1, 2 ), new Dimensions( 3, 4 ), mock( WidgetLayout.class ) );
	}
	
	@Test
	public void constructor() {
		assertEquals( new Location( 1, 2 ), root.getLocation() );
		assertEquals( new Dimensions( 3, 4 ), root.getDimensions() );
	}
	
	@Test
	public void setWindow() {
		root.setLocation( new Location( 6, 7 ) );
		root.setDimensions( new Dimensions( 8, 9 ) );
		assertEquals( new Location( 6, 7 ), root.getLocation() );
		assertEquals( new Dimensions( 8, 9 ), root.getDimensions() );
	}
}
