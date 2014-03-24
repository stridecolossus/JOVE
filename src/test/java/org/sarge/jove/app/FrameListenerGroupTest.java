package org.sarge.jove.app;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;

public class FrameListenerGroupTest {
	private FrameListenerGroup group;
	private FrameListener listener;

	@Before
	public void before() {
		group = new FrameListenerGroup();
		listener = mock( FrameListener.class );
		group.add( listener );
	}

	@Test
	public void add() {
		group.update( 1, 2 );
		verify( listener ).update( 1, 2 );
	}

	@Test
	public void remove() {
		group.remove( listener );
		group.update( 1, 2 );
		verifyZeroInteractions( listener );
	}

	@Test
	public void clear() {
		group.clear();
		group.update( 1, 2 );
		verifyZeroInteractions( listener );
	}
}
