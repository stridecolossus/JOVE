package org.sarge.jove.widget;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class LabelWidgetTest {
	private LabelWidget widget;
	private Drawable label;
	
	@Before
	public void before() {
		label = mock( Drawable.class );
		widget = new LabelWidget( label );
	}
	
	@Test
	public void getDimensions() {
		final Dimensions dim = new Dimensions( 1, 2 );
		when( label.getDimensions() ).thenReturn( dim );
		assertEquals( dim, label.getDimensions() );
	}
	
	@Test
	public void setLabel() {
		widget.setLabel( label );
	}
}
