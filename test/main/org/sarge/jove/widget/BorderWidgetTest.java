package org.sarge.jove.widget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;

public class BorderWidgetTest {
	private BorderWidget widget;
	private Widget child;
	private Border border;
	
	@Before
	public void before() {
		// Create enclosed widget
		child = mock( Widget.class );
		when( child.getDimensions() ).thenReturn( new Dimensions( 4, 5 ) );
		
		// Create border
		border = mock( Border.class );
		when( border.getWidthInset() ).thenReturn( 2f );
		when( border.getHeightInset() ).thenReturn( 3f );

		// Create widget
		widget = new BorderWidget( border, child );
	}
	
	@Test
	public void constructor() {
		assertEquals( Collections.singletonList( child ), widget.getChildren() );
	}
	
	@Test
	public void getDimensions() {
		assertEquals( new Dimensions( ( 2 * 2 ) + 4, ( 2 * 3 ) + 5 ), widget.getDimensions() );
	}
	
	@Test
	public void setBorder() {
		border = mock( Border.class );
		widget.setBorder( border );
		assertEquals( "Expected zero-size border", child.getDimensions(), widget.getDimensions() );
	}
	
	@Test
	public void setWidget() {
		child = mock( Widget.class );
		when( child.getDimensions() ).thenReturn( new Dimensions() );
		widget.setWidget( child );
		assertEquals( "Expected zero-size widget", new Dimensions( 2 * 2, 2 * 3 ), widget.getDimensions() );
	}
	
	@Test( expected = IllegalArgumentException.class )
	public void setWidgetSelf() {
		widget.setWidget( widget );
	}
}
